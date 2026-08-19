package com.llacsaa.timesheet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una fila de la sección "Hitos" del reporte de avance: actividades "Hijo"
 * de tprj_project_schedule (memberuser no nulo), agrupadas por su fase
 * (Padre), con las mismas métricas de días/% que PhaseProgressRow.
 */
@Data
@AllArgsConstructor
public class MilestoneRow {
    private Long seqschedule;
    private Long parentSeqschedule;
    private String parentDescription;
    private String description;
    private String memberName;

    private BigDecimal baseDays;
    private BigDecimal addendumDays;
    private BigDecimal totalDays;
    private BigDecimal expectedAdvanceDays;
    private BigDecimal realAdvanceDays;
    private BigDecimal plannedAssignmentVariationPerc;
    private BigDecimal realAdvancePerc;
    private BigDecimal effectivenessPerc;
    private BigDecimal daysConsumedTs;

    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
}
