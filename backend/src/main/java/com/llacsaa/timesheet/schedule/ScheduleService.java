package com.llacsaa.timesheet.schedule;

import com.llacsaa.timesheet.common.NameResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;
import com.llacsaa.timesheet.auth.AuthContext;

/**
 * Reglas de "Pantalla CRUD de Schedule de Proyecto.docx":
 *   - Padre/Hermano/Hijo se resuelve en la creación a partir de una
 *     actividad de referencia; parent/seq se generan automáticamente.
 *   - Fila "Padre": solo Descripción/Descripción larga son editables (no
 *     tiene responsable/días/fechas). Se detecta como Padre cuando
 *     memberuser es null (no hay columna de tipo persistida — se deriva).
 *   - Fila "Hijo": Descripción, Descripción larga, Responsable, Días Base,
 *     Días Adicional, Inicio/Fin base son editables; el resto es solo lectura.
 *   - Eliminar: bloqueado si el registro tiene hijas (seqscheduleparent
 *     apuntando a él).
 */
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleHisRepository scheduleHisRepository;
    private final NameResolver nameResolver;

    public ScheduleService(ScheduleRepository scheduleRepository,
                            ScheduleHisRepository scheduleHisRepository,
                            NameResolver nameResolver) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleHisRepository = scheduleHisRepository;
        this.nameResolver = nameResolver;
    }

    public List<ScheduleView> listByProject(Long seqproject) {
        return scheduleRepository.findBySeqprojectOrderBySeqschedule(seqproject)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    @Transactional
    public ScheduleView create(Long seqproject, ScheduleCreateRequest request) {
        if (request.getHierarchyType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hierarchyType es requerido (PADRE, HERMANO o HIJO)");
        }

        Long parentSeq;
        boolean asPadre;

        switch (request.getHierarchyType()) {
            case HIJO: {
                TprjProjectSchedule reference = requireReference(seqproject, request.getReferenceSeqschedule(), "HIJO");
                parentSeq = reference.getSeqschedule();
                asPadre = false;
                break;
            }
            case HERMANO: {
                TprjProjectSchedule reference = requireReference(seqproject, request.getReferenceSeqschedule(), "HERMANO");
                parentSeq = reference.getSeqscheduleparent();
                asPadre = reference.getMemberuser() == null;
                break;
            }
            case PADRE:
            default: {
                if (request.getReferenceSeqschedule() != null) {
                    TprjProjectSchedule reference = requireReference(seqproject, request.getReferenceSeqschedule(), "PADRE");
                    parentSeq = reference.getSeqschedule();
                } else {
                    parentSeq = null;
                }
                asPadre = true;
                break;
            }
        }

        TprjProjectSchedule s = new TprjProjectSchedule();
        s.setCodeinstance(CODEINSTANCE);
        s.setCodecompany(CODECOMPANY);
        s.setSeqproject(seqproject);
        s.setSeqscheduleparent(parentSeq);
        s.setShortactivitydesc(request.getShortactivitydesc());
        s.setActivitydesc(request.getActivitydesc());

        if (!asPadre) {
            applyHijoFields(s, request.getMemberuser(), request.getBasedays(), request.getBaseadicional(),
                    request.getBaseStartDate(), request.getBaseEndDate());
        }

        s.setUsercreate(AuthContext.currentUserCode());
        s.setUserlastmodify(AuthContext.currentUserCode());
        s.setDatecreate(LocalDateTime.now());
        s.setDatemodify(LocalDateTime.now());

        TprjProjectSchedule saved = scheduleRepository.save(s);
        scheduleHisRepository.save(TprjProjectScheduleHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public ScheduleView update(Long seqproject, Long seqschedule, ScheduleUpdateRequest request) {
        TprjProjectSchedule s = findOrThrow(seqproject, seqschedule);
        boolean isPadre = s.getMemberuser() == null;

        s.setShortactivitydesc(request.getShortactivitydesc());
        s.setActivitydesc(request.getActivitydesc());

        if (!isPadre) {
            applyHijoFields(s, request.getMemberuser(), request.getBasedays(), request.getBaseadicional(),
                    request.getBaseStartDate(), request.getBaseEndDate());
        }

        s.setUserlastmodify(AuthContext.currentUserCode());
        s.setDatemodify(LocalDateTime.now());

        TprjProjectSchedule saved = scheduleRepository.save(s);
        scheduleHisRepository.save(TprjProjectScheduleHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public void delete(Long seqproject, Long seqschedule) {
        TprjProjectSchedule s = findOrThrow(seqproject, seqschedule);
        if (!scheduleRepository.findBySeqscheduleparent(seqschedule).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta actividad tiene actividades dependientes; reasígnelas o elimínelas primero");
        }
        scheduleHisRepository.save(TprjProjectScheduleHis.snapshotOf(s, "DELETE", AuthContext.currentUserCode()));
        scheduleRepository.delete(s);
    }

    private void applyHijoFields(TprjProjectSchedule s, Long memberuser, BigDecimal basedays, BigDecimal baseadicional,
                                  java.time.LocalDate baseStartDate, java.time.LocalDate baseEndDate) {
        s.setMemberuser(memberuser);
        BigDecimal base = basedays == null ? BigDecimal.ZERO : basedays;
        BigDecimal adicional = baseadicional == null ? BigDecimal.ZERO : baseadicional;
        s.setBasedays(base);
        s.setBaseadicional(adicional);
        s.setBasedaystotal(base.add(adicional));
        s.setBaseStartDate(baseStartDate);
        s.setBaseEndDate(baseEndDate);
    }

    private TprjProjectSchedule requireReference(Long seqproject, Long referenceSeqschedule, String hierarchyType) {
        if (referenceSeqschedule == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "referenceSeqschedule es requerido para hierarchyType=" + hierarchyType);
        }
        return findOrThrow(seqproject, referenceSeqschedule);
    }

    private TprjProjectSchedule findOrThrow(Long seqproject, Long seqschedule) {
        return scheduleRepository.findById(seqschedule)
                .filter(s -> s.getSeqproject().equals(seqproject))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
    }

    private ScheduleView toView(TprjProjectSchedule s) {
        return new ScheduleView(
                s.getSeqschedule(),
                s.getSeqscheduleparent(),
                s.getShortactivitydesc(),
                s.getActivitydesc(),
                s.getMemberuser(),
                nameResolver.userName(s.getMemberuser()),
                s.getBasedays(),
                s.getBaseadicional(),
                s.getBasedaystotal(),
                s.getBaseStartDate(),
                s.getBaseEndDate(),
                s.getEfectivityperc(),
                s.getAdvrealperc(),
                s.getMemberuser() == null
        );
    }
}
