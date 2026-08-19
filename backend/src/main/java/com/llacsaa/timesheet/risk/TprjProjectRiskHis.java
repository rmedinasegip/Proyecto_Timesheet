package com.llacsaa.timesheet.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_project_risk_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectRiskHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;
    private Long seqrisk;

    private LocalDate riskdate;
    private String risktypeimpactcat;
    private String risktypeimpact;
    private String riskdescimpact;
    private String personincharge;
    private String company;
    private String solution;
    private BigDecimal probabilityperc;
    private String riskstatuscat;
    private String riskstatus;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectRiskHis snapshotOf(TprjProjectRisk r, String actiondml, long userchange) {
        TprjProjectRiskHis h = new TprjProjectRiskHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(r.getCodeinstance());
        h.setCodecompany(r.getCodecompany());
        h.setSeqproject(r.getSeqproject());
        h.setSeqrisk(r.getSeqrisk());
        h.setRiskdate(r.getRiskdate());
        h.setRisktypeimpactcat(r.getRisktypeimpactcat());
        h.setRisktypeimpact(r.getRisktypeimpact());
        h.setRiskdescimpact(r.getRiskdescimpact());
        h.setPersonincharge(r.getPersonincharge());
        h.setCompany(r.getCompany());
        h.setSolution(r.getSolution());
        h.setProbabilityperc(r.getProbabilityperc());
        h.setRiskstatuscat(r.getRiskstatuscat());
        h.setRiskstatus(r.getRiskstatus());
        h.setUsercreate(r.getUsercreate());
        h.setUserlastmodify(r.getUserlastmodify());
        h.setDatecreate(r.getDatecreate());
        h.setDatemodify(r.getDatemodify());
        return h;
    }
}
