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
@Table(name = "tprj_project_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqschedule;

    private String codeinstance;
    private String codecompany;
    private String companyallocate;
    private Long seqproject;
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
}
