package com.llacsaa.timesheet.timesheet;

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
import java.time.LocalTime;

@Entity
@Table(name = "tprj_project_timesheet_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTimesheetHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqts;

    private Long memberuser;
    private String codecompanyconsultant;
    private Long codecustomer;
    private Long seqproject;

    private String systemcat;
    private String system;
    private String modulecat;
    private String module;
    private String sprint;
    private String incidentref;
    private String activitytypecat;
    private String activitytype;
    private String activitydesc;

    private LocalDate tsdate;
    private LocalTime starttime;
    private LocalTime endtime;
    private BigDecimal hoursconsumed;

    private String statuscat;
    private String status;
    private Long reviewedby;
    private Long seqschedule;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectTimesheetHis snapshotOf(TprjProjectTimesheet t, String actiondml, long userchange) {
        TprjProjectTimesheetHis h = new TprjProjectTimesheetHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(t.getCodeinstance());
        h.setCodecompany(t.getCodecompany());
        h.setSeqts(t.getSeqts());
        h.setMemberuser(t.getMemberuser());
        h.setCodecompanyconsultant(t.getCodecompanyconsultant());
        h.setCodecustomer(t.getCodecustomer());
        h.setSeqproject(t.getSeqproject());
        h.setSystemcat(t.getSystemcat());
        h.setSystem(t.getSystem());
        h.setModulecat(t.getModulecat());
        h.setModule(t.getModule());
        h.setSprint(t.getSprint());
        h.setIncidentref(t.getIncidentref());
        h.setActivitytypecat(t.getActivitytypecat());
        h.setActivitytype(t.getActivitytype());
        h.setActivitydesc(t.getActivitydesc());
        h.setTsdate(t.getTsdate());
        h.setStarttime(t.getStarttime());
        h.setEndtime(t.getEndtime());
        h.setHoursconsumed(t.getHoursconsumed());
        h.setStatuscat(t.getStatuscat());
        h.setStatus(t.getStatus());
        h.setReviewedby(t.getReviewedby());
        h.setSeqschedule(t.getSeqschedule());
        h.setUsercreate(t.getUsercreate());
        h.setUserlastmodify(t.getUserlastmodify());
        h.setDatecreate(t.getDatecreate());
        h.setDatemodify(t.getDatemodify());
        return h;
    }
}
