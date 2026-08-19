package com.llacsaa.timesheet.timesheet;

import com.llacsaa.timesheet.common.NameResolver;
import com.llacsaa.timesheet.project.ProjectRepository;
import com.llacsaa.timesheet.project.TprjProject;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;
import com.llacsaa.timesheet.auth.AuthContext;

/**
 * Reglas (Excel "Registro diario" + plan, sin documento CRUD dedicado):
 *   - hoursconsumed se calcula siempre en el servidor a partir de
 *     starttime/endtime — nunca se acepta del cliente.
 *   - statuscat/status inicia en PRJ_TIMESHEETSTATUSCAT/'REG' (Registrado)
 *     al crear; solo cambia vía {@link #review}, no vía {@link #update}
 *     (separa la edición del propio consultor de la revisión del
 *     autorizador). Desde Fase 5, {@link #review} exige rol AUT
 *     (Autorizador) — ver {@code AuthContext#isAuthorizer()}.
 */
@Service
public class TimesheetService {

    public static final String SYSTEMCAT = "PRJ_SYSTEMSCAT";
    public static final String MODULECAT = "PRJ_MODULECAT";
    public static final String ACTIVITYTYPECAT = "PRJ_ACTIVITYTYTYPECAT";
    public static final String STATUSCAT = "PRJ_TIMESHEETSTATUSCAT";
    private static final String DEFAULT_STATUS = "REG";

    private final TimesheetRepository timesheetRepository;
    private final TimesheetHisRepository timesheetHisRepository;
    private final ProjectRepository projectRepository;
    private final NameResolver nameResolver;

    public TimesheetService(TimesheetRepository timesheetRepository,
                             TimesheetHisRepository timesheetHisRepository,
                             ProjectRepository projectRepository,
                             NameResolver nameResolver) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHisRepository = timesheetHisRepository;
        this.projectRepository = projectRepository;
        this.nameResolver = nameResolver;
    }

    public List<TimesheetView> search(Long memberuser, LocalDate dateFrom, LocalDate dateTo, String status, Long seqproject) {
        Specification<TprjProjectTimesheet> spec = Specification
                .where(TimesheetSpecifications.context(CODEINSTANCE, CODECOMPANY))
                .and(TimesheetSpecifications.memberuser(memberuser))
                .and(TimesheetSpecifications.dateFrom(dateFrom))
                .and(TimesheetSpecifications.dateTo(dateTo))
                .and(TimesheetSpecifications.status(status))
                .and(TimesheetSpecifications.seqproject(seqproject));

        Sort sort = Sort.by(Sort.Direction.DESC, "tsdate").and(Sort.by(Sort.Direction.DESC, "seqts"));
        return timesheetRepository.findAll(spec, sort)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    @Transactional
    public TimesheetView create(TimesheetRequest request) {
        TprjProjectTimesheet t = new TprjProjectTimesheet();
        t.setCodeinstance(CODEINSTANCE);
        t.setCodecompany(CODECOMPANY);
        applyRequest(t, request);
        t.setStatuscat(STATUSCAT);
        t.setStatus(DEFAULT_STATUS);
        t.setUsercreate(AuthContext.currentUserCode());
        t.setUserlastmodify(AuthContext.currentUserCode());
        t.setDatecreate(LocalDateTime.now());
        t.setDatemodify(LocalDateTime.now());

        TprjProjectTimesheet saved = timesheetRepository.save(t);
        timesheetHisRepository.save(TprjProjectTimesheetHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public TimesheetView update(Long seqts, TimesheetRequest request) {
        TprjProjectTimesheet t = findOrThrow(seqts);
        applyRequest(t, request);
        t.setUserlastmodify(AuthContext.currentUserCode());
        t.setDatemodify(LocalDateTime.now());

        TprjProjectTimesheet saved = timesheetRepository.save(t);
        timesheetHisRepository.save(TprjProjectTimesheetHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public TimesheetView review(Long seqts, TimesheetReviewRequest request) {
        if (!AuthContext.isAuthorizer()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un autorizador puede aprobar/rechazar");
        }
        if (!"APR".equals(request.getStatus()) && !"REC".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status debe ser APR o REC");
        }
        TprjProjectTimesheet t = findOrThrow(seqts);
        t.setStatus(request.getStatus());
        t.setReviewedby(request.getReviewedby());
        t.setUserlastmodify(AuthContext.currentUserCode());
        t.setDatemodify(LocalDateTime.now());

        TprjProjectTimesheet saved = timesheetRepository.save(t);
        timesheetHisRepository.save(TprjProjectTimesheetHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public void delete(Long seqts) {
        TprjProjectTimesheet t = findOrThrow(seqts);
        timesheetHisRepository.save(TprjProjectTimesheetHis.snapshotOf(t, "DELETE", AuthContext.currentUserCode()));
        timesheetRepository.delete(t);
    }

    private TprjProjectTimesheet findOrThrow(Long seqts) {
        return timesheetRepository.findById(seqts)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de timesheet no encontrado"));
    }

    private void applyRequest(TprjProjectTimesheet t, TimesheetRequest r) {
        if (r.getStarttime() == null || r.getEndtime() == null || !r.getEndtime().isAfter(r.getStarttime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora fin debe ser posterior a la hora inicio");
        }
        t.setMemberuser(r.getMemberuser());
        t.setCodecompanyconsultant(r.getCodecompanyconsultant());
        t.setCodecustomer(r.getCodecustomer());
        t.setSeqproject(r.getSeqproject());
        t.setSystemcat(SYSTEMCAT);
        t.setSystem(r.getSystem());
        t.setModulecat(MODULECAT);
        t.setModule(r.getModule());
        t.setSprint(r.getSprint());
        t.setIncidentref(r.getIncidentref());
        t.setActivitytypecat(ACTIVITYTYPECAT);
        t.setActivitytype(r.getActivitytype());
        t.setActivitydesc(r.getActivitydesc());
        t.setTsdate(r.getTsdate());
        t.setStarttime(r.getStarttime());
        t.setEndtime(r.getEndtime());
        t.setSeqschedule(r.getSeqschedule());

        Duration duration = Duration.between(r.getStarttime(), r.getEndtime());
        BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        t.setHoursconsumed(hours);
    }

    private TimesheetView toView(TprjProjectTimesheet t) {
        String projectName = Optional.ofNullable(t.getSeqproject())
                .flatMap(projectRepository::findById)
                .map(TprjProject::getProjectName)
                .orElse(null);

        return new TimesheetView(
                t.getSeqts(),
                t.getMemberuser(),
                nameResolver.userName(t.getMemberuser()),
                t.getCodecompanyconsultant(),
                nameResolver.companyName(t.getCodecompanyconsultant()),
                t.getCodecustomer(),
                nameResolver.customerName(t.getCodecustomer()),
                t.getSeqproject(),
                projectName,
                t.getSystem(),
                nameResolver.catalogItemName(t.getSystemcat(), t.getSystem()),
                t.getModule(),
                nameResolver.catalogItemName(t.getModulecat(), t.getModule()),
                t.getSprint(),
                t.getIncidentref(),
                t.getActivitytype(),
                nameResolver.catalogItemName(t.getActivitytypecat(), t.getActivitytype()),
                t.getActivitydesc(),
                t.getTsdate(),
                t.getStarttime(),
                t.getEndtime(),
                t.getHoursconsumed(),
                t.getStatus(),
                nameResolver.catalogItemName(t.getStatuscat(), t.getStatus()),
                t.getReviewedby(),
                nameResolver.userName(t.getReviewedby()),
                t.getSeqschedule()
        );
    }
}
