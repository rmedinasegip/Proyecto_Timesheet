package com.llacsaa.timesheet.weeklyprogress;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WeeklyProgressActivityView {
    private Long seqproject;
    private String projectName;
    private Long seqschedule;
    private String shortactivitydesc;
    private String activitydesc;
    private Long memberuser;
    private String memberName;

    /** Avance real de la actividad tal cual está en tprj_project_schedule antes de tocar esta semana. */
    private BigDecimal currentAdvancePerc;

    /** Null si la semana pedida todavía no tiene registro. */
    private Long seqtsweek;
    private BigDecimal day1Perc;
    private BigDecimal day2Perc;
    private BigDecimal day3Perc;
    private BigDecimal day4Perc;
    private BigDecimal day5Perc;
    private BigDecimal day6Perc;
    private BigDecimal day7Perc;
    private String status;
    private String statusName;
    private Long reviewedby;
    private String reviewedbyName;
}
