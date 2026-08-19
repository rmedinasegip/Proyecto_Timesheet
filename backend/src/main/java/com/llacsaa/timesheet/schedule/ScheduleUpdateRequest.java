package com.llacsaa.timesheet.schedule;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleUpdateRequest {
    private String shortactivitydesc;
    private String activitydesc;

    // Solo se aplican si la fila editada es de tipo "Hijo" (ver reglas de negocio)
    private Long memberuser;
    private BigDecimal basedays;
    private BigDecimal baseadicional;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
}
