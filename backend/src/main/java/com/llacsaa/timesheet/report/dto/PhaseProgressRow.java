package com.llacsaa.timesheet.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Una fila de la sección "Días del proyecto por fase" del reporte de avance
 * (hoja "Detalle" del Excel). Cada fila corresponde a una actividad "Padre"
 * de tprj_project_schedule (una fase); la fila con seqschedule == null es la
 * fila TOTAL, agregada a nivel de todo el proyecto — ver
 * ProjectProgressReportService para el detalle de qué columnas se suman
 * entre fases y cuáles se toman directo de tprj_project.
 */
@Data
@AllArgsConstructor
public class PhaseProgressRow {
    private Long seqschedule;
    private String description;

    private BigDecimal contractedDays;          // Días Contratados (anexo) — schedule.basedays
    private BigDecimal addendumDays;             // Días adicionales aprobados (adendum) — schedule.baseadicional
    private BigDecimal totalProjectDays;         // Total días proyecto — schedule.basedaystotal
    private BigDecimal expectedAdvanceDays;      // Avance esperado (d) (calculado: contractedDays * plannedAssignmentVariationPerc / 100; null si plannedAssignmentVariationPerc es null)
    private BigDecimal realAdvanceDays;          // Días avance real actual — schedule.advrealdays
    private BigDecimal daysToInvest;             // Días por invertir (calculado: totalProjectDays - realAdvanceDays)
    private BigDecimal advanceVariationPerc;     // % Variación avance proyecto (calculado: realAdvancePerc - expectedAdvancePerc)
    private BigDecimal daysConsumedTs;           // Días T.S. (calculado: fecha de corte - project.realStartDate; mismo valor en toda fila, no por fase — null si el proyecto no tiene fecha inicio real)
    private BigDecimal balanceDaysTs;            // Saldo T.S. (calculado: totalProjectDays - daysConsumedTs; null si daysConsumedTs es null)
    private BigDecimal plannedAssignmentVariationPerc; // % Var. asig. planif. (calculado: realAdvanceDays / totalProjectDays * 100; null si totalProjectDays es 0)
    private BigDecimal currentAdvancePerc;       // % Avance Actual — advexpectedperc
    private BigDecimal realAdvancePerc;          // % Avance Real — advrealperc
    private BigDecimal effectivenessPerc;        // % Efectividad (calculado: realAdvancePerc / plannedAssignmentVariationPerc; null si el denominador es 0/null)

    private boolean total;
}
