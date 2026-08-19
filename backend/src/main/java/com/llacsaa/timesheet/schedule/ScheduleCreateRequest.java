package com.llacsaa.timesheet.schedule;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleCreateRequest {
    private HierarchyType hierarchyType;
    /** Actividad de referencia: requerida para HERMANO/HIJO; opcional para PADRE (sub-fase anidada). */
    private Long referenceSeqschedule;

    private String shortactivitydesc;
    private String activitydesc;

    // Solo aplican si el resultado es una fila "Hijo" (ver HierarchyType/reglas de negocio)
    private Long memberuser;
    private BigDecimal basedays;
    private BigDecimal baseadicional;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
}
