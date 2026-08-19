package com.llacsaa.timesheet.change;

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
@Table(name = "tprj_project_change_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectChangeHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;
    private Long seqchange;

    private LocalDate changedate;
    private String phase;
    private String deliverable;
    private String reason;
    private String consequence;
    private String approvedby;
    private String company;
    private BigDecimal daysvariation;
    private LocalDate plannedapplydate;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectChangeHis snapshotOf(TprjProjectChange c, String actiondml, long userchange) {
        TprjProjectChangeHis h = new TprjProjectChangeHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(c.getCodeinstance());
        h.setCodecompany(c.getCodecompany());
        h.setSeqproject(c.getSeqproject());
        h.setSeqchange(c.getSeqchange());
        h.setChangedate(c.getChangedate());
        h.setPhase(c.getPhase());
        h.setDeliverable(c.getDeliverable());
        h.setReason(c.getReason());
        h.setConsequence(c.getConsequence());
        h.setApprovedby(c.getApprovedby());
        h.setCompany(c.getCompany());
        h.setDaysvariation(c.getDaysvariation());
        h.setPlannedapplydate(c.getPlannedapplydate());
        h.setUsercreate(c.getUsercreate());
        h.setUserlastmodify(c.getUserlastmodify());
        h.setDatecreate(c.getDatecreate());
        h.setDatemodify(c.getDatemodify());
        return h;
    }
}
