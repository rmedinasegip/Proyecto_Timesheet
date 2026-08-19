package com.llacsaa.timesheet.change;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;
import com.llacsaa.timesheet.auth.AuthContext;

/**
 * "Cambios Aprobados" — sección del reporte de avance (Fase 4), sin
 * documento CRUD dedicado (ver V5__project_change_module.sql). CRUD idéntico
 * en estructura a RiskService/NewsService.
 */
@Service
public class ChangeService {

    private final ChangeRepository changeRepository;
    private final ChangeHisRepository changeHisRepository;

    public ChangeService(ChangeRepository changeRepository, ChangeHisRepository changeHisRepository) {
        this.changeRepository = changeRepository;
        this.changeHisRepository = changeHisRepository;
    }

    public List<ChangeView> listByProject(Long seqproject) {
        return changeRepository.findBySeqprojectOrderByChangedateDesc(seqproject)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    @Transactional
    public ChangeView create(Long seqproject, ChangeRequest request) {
        TprjProjectChange c = new TprjProjectChange();
        c.setCodeinstance(CODEINSTANCE);
        c.setCodecompany(CODECOMPANY);
        c.setSeqproject(seqproject);
        applyRequest(c, request);
        c.setUsercreate(AuthContext.currentUserCode());
        c.setUserlastmodify(AuthContext.currentUserCode());
        c.setDatecreate(LocalDateTime.now());
        c.setDatemodify(LocalDateTime.now());

        TprjProjectChange saved = changeRepository.save(c);
        changeHisRepository.save(TprjProjectChangeHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public ChangeView update(Long seqproject, Long seqchange, ChangeRequest request) {
        TprjProjectChange c = findOrThrow(seqproject, seqchange);
        applyRequest(c, request);
        c.setUserlastmodify(AuthContext.currentUserCode());
        c.setDatemodify(LocalDateTime.now());

        TprjProjectChange saved = changeRepository.save(c);
        changeHisRepository.save(TprjProjectChangeHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public void delete(Long seqproject, Long seqchange) {
        TprjProjectChange c = findOrThrow(seqproject, seqchange);
        changeHisRepository.save(TprjProjectChangeHis.snapshotOf(c, "DELETE", AuthContext.currentUserCode()));
        changeRepository.delete(c);
    }

    private TprjProjectChange findOrThrow(Long seqproject, Long seqchange) {
        return changeRepository.findById(seqchange)
                .filter(c -> c.getSeqproject().equals(seqproject))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cambio aprobado no encontrado"));
    }

    private void applyRequest(TprjProjectChange c, ChangeRequest request) {
        c.setChangedate(request.getChangedate());
        c.setPhase(request.getPhase());
        c.setDeliverable(request.getDeliverable());
        c.setReason(request.getReason());
        c.setConsequence(request.getConsequence());
        c.setApprovedby(request.getApprovedby());
        c.setCompany(request.getCompany());
        c.setDaysvariation(request.getDaysvariation());
        c.setPlannedapplydate(request.getPlannedapplydate());
    }

    private ChangeView toView(TprjProjectChange c) {
        return new ChangeView(
                c.getSeqchange(),
                c.getChangedate(),
                c.getPhase(),
                c.getDeliverable(),
                c.getReason(),
                c.getConsequence(),
                c.getApprovedby(),
                c.getCompany(),
                c.getDaysvariation(),
                c.getPlannedapplydate()
        );
    }
}
