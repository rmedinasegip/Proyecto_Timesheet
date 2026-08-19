package com.llacsaa.timesheet.schedule;

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
@Table(name = "tprj_project_schedule_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectScheduleHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private String companyallocate;
    private Long seqproject;
    private Long seqschedule;
    private Long seqscheduleparent;
    private Long lastseqts;

    private String systemcat;
    private String system;
    private Long memberuser;
    private String sprint;
    private String servicecode;
    private String activitytypecat;
    private String activitytype;
    private String shortactivitydesc;
    private String activitydesc;

    private BigDecimal advexpecteddays;
    private BigDecimal advrealdays;
    private BigDecimal advexpectedperc;
    private BigDecimal advrealperc;
    private BigDecimal advrealnoconfirmperc;
    private BigDecimal basedays;
    private BigDecimal baseadicional;
    private BigDecimal basedaystotal;
    private BigDecimal planneddays;
    private BigDecimal realdays;
    private BigDecimal varadvplannedperc;
    private BigDecimal efectivityperc;

    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate realStartDate;
    private LocalDate realEndDate;

    private BigDecimal daysconsumedts;
    private BigDecimal daysconsumedaut;
    private LocalDate datestamentday;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectScheduleHis snapshotOf(TprjProjectSchedule s, String actiondml, long userchange) {
        TprjProjectScheduleHis h = new TprjProjectScheduleHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(s.getCodeinstance());
        h.setCodecompany(s.getCodecompany());
        h.setCompanyallocate(s.getCompanyallocate());
        h.setSeqproject(s.getSeqproject());
        h.setSeqschedule(s.getSeqschedule());
        h.setSeqscheduleparent(s.getSeqscheduleparent());
        h.setLastseqts(s.getLastseqts());
        h.setSystemcat(s.getSystemcat());
        h.setSystem(s.getSystem());
        h.setMemberuser(s.getMemberuser());
        h.setSprint(s.getSprint());
        h.setServicecode(s.getServicecode());
        h.setActivitytypecat(s.getActivitytypecat());
        h.setActivitytype(s.getActivitytype());
        h.setShortactivitydesc(s.getShortactivitydesc());
        h.setActivitydesc(s.getActivitydesc());
        h.setAdvexpecteddays(s.getAdvexpecteddays());
        h.setAdvrealdays(s.getAdvrealdays());
        h.setAdvexpectedperc(s.getAdvexpectedperc());
        h.setAdvrealperc(s.getAdvrealperc());
        h.setAdvrealnoconfirmperc(s.getAdvrealnoconfirmperc());
        h.setBasedays(s.getBasedays());
        h.setBaseadicional(s.getBaseadicional());
        h.setBasedaystotal(s.getBasedaystotal());
        h.setPlanneddays(s.getPlanneddays());
        h.setRealdays(s.getRealdays());
        h.setVaradvplannedperc(s.getVaradvplannedperc());
        h.setEfectivityperc(s.getEfectivityperc());
        h.setBaseStartDate(s.getBaseStartDate());
        h.setBaseEndDate(s.getBaseEndDate());
        h.setPlannedStartDate(s.getPlannedStartDate());
        h.setPlannedEndDate(s.getPlannedEndDate());
        h.setRealStartDate(s.getRealStartDate());
        h.setRealEndDate(s.getRealEndDate());
        h.setDaysconsumedts(s.getDaysconsumedts());
        h.setDaysconsumedaut(s.getDaysconsumedaut());
        h.setDatestamentday(s.getDatestamentday());
        h.setUsercreate(s.getUsercreate());
        h.setUserlastmodify(s.getUserlastmodify());
        h.setDatecreate(s.getDatecreate());
        h.setDatemodify(s.getDatemodify());
        return h;
    }
}
