package com.llacsaa.timesheet.risk;

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

@Service
public class RiskService {

    public static final String RISKTYPEIMPACTCAT = "PRJ_RISKTYPEIMPACTCAT";
    public static final String RISKSTATUSCAT = "PRJ_RISKSTATUSCAT";
    private static final String DEFAULT_STATUS = "ABI";

    private final RiskRepository riskRepository;
    private final RiskHisRepository riskHisRepository;
    private final NameResolver nameResolver;

    public RiskService(RiskRepository riskRepository, RiskHisRepository riskHisRepository, NameResolver nameResolver) {
        this.riskRepository = riskRepository;
        this.riskHisRepository = riskHisRepository;
        this.nameResolver = nameResolver;
    }

    public List<RiskView> listByProject(Long seqproject) {
        return riskRepository.findBySeqprojectOrderByRiskdateDesc(seqproject)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    @Transactional
    public RiskView create(Long seqproject, RiskRequest request) {
        TprjProjectRisk r = new TprjProjectRisk();
        r.setCodeinstance(CODEINSTANCE);
        r.setCodecompany(CODECOMPANY);
        r.setSeqproject(seqproject);
        applyRequest(r, request);
        if (r.getRiskstatus() == null) {
            r.setRiskstatus(DEFAULT_STATUS);
        }
        r.setUsercreate(AuthContext.currentUserCode());
        r.setUserlastmodify(AuthContext.currentUserCode());
        r.setDatecreate(LocalDateTime.now());
        r.setDatemodify(LocalDateTime.now());

        TprjProjectRisk saved = riskRepository.save(r);
        riskHisRepository.save(TprjProjectRiskHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public RiskView update(Long seqproject, Long seqrisk, RiskRequest request) {
        TprjProjectRisk r = findOrThrow(seqproject, seqrisk);
        applyRequest(r, request);
        r.setUserlastmodify(AuthContext.currentUserCode());
        r.setDatemodify(LocalDateTime.now());

        TprjProjectRisk saved = riskRepository.save(r);
        riskHisRepository.save(TprjProjectRiskHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public void delete(Long seqproject, Long seqrisk) {
        TprjProjectRisk r = findOrThrow(seqproject, seqrisk);
        riskHisRepository.save(TprjProjectRiskHis.snapshotOf(r, "DELETE", AuthContext.currentUserCode()));
        riskRepository.delete(r);
    }

    private BigDecimal validateProbability(BigDecimal probabilityperc) {
        if (probabilityperc == null) {
            return null;
        }
        if (probabilityperc.compareTo(BigDecimal.ZERO) < 0 || probabilityperc.compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La probabilidad debe estar entre 0 y 100");
        }
        return probabilityperc;
    }

    private TprjProjectRisk findOrThrow(Long seqproject, Long seqrisk) {
        return riskRepository.findById(seqrisk)
                .filter(r -> r.getSeqproject().equals(seqproject))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Riesgo no encontrado"));
    }

    private void applyRequest(TprjProjectRisk r, RiskRequest request) {
        r.setRiskdate(request.getRiskdate());
        r.setRisktypeimpactcat(RISKTYPEIMPACTCAT);
        r.setRisktypeimpact(request.getRisktypeimpact());
        r.setRiskdescimpact(request.getRiskdescimpact());
        r.setPersonincharge(request.getPersonincharge());
        r.setCompany(request.getCompany());
        r.setSolution(request.getSolution());
        r.setProbabilityperc(validateProbability(request.getProbabilityperc()));
        r.setRiskstatuscat(RISKSTATUSCAT);
        if (request.getRiskstatus() != null) {
            r.setRiskstatus(request.getRiskstatus());
        }
    }

    private RiskView toView(TprjProjectRisk r) {
        return new RiskView(
                r.getSeqrisk(),
                r.getRiskdate(),
                r.getRisktypeimpactcat(),
                r.getRisktypeimpact(),
                nameResolver.catalogItemName(r.getRisktypeimpactcat(), r.getRisktypeimpact()),
                r.getRiskdescimpact(),
                r.getPersonincharge(),
                r.getCompany(),
                r.getSolution(),
                r.getProbabilityperc(),
                r.getRiskstatuscat(),
                r.getRiskstatus(),
                nameResolver.catalogItemName(r.getRiskstatuscat(), r.getRiskstatus())
        );
    }
}
