package com.llacsaa.timesheet.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ScheduleView {
    private Long seqschedule;
    private Long seqscheduleparent;
    private String shortactivitydesc;
    private String activitydesc;
    private Long memberuser;
    private String memberName;
    private BigDecimal basedays;
    private BigDecimal baseadicional;
    private BigDecimal basedaystotal;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private BigDecimal efectivityperc;
    private BigDecimal advrealperc;
    /** true = fila "Padre" (solo descripción editable); false = fila "Hijo" (campos completos editables). */
    private boolean padre;
}
