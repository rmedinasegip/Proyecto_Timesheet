package com.llacsaa.timesheet.project.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * "Registrar avance" (Fase 4, pestaña Avance): a diferencia del resto del
 * modal de Proyecto, este único grupo de campos de tprj_project se edita
 * mediante su propio endpoint (PUT /api/projects/{seq}/progress) en vez del
 * PUT general de cabecera — refleja que es una acción de negocio distinta
 * (registrar un corte de avance), no una edición de los datos generales del
 * proyecto, mismo criterio que separó review() de update() en Timesheets
 * (Fase 3).
 */
@Data
public class ProgressRequest {
    private LocalDate lastcutoffdate;
    private BigDecimal advexpectedperc;
    private BigDecimal advrealperc;
    private BigDecimal advexpecteddays;
    private BigDecimal advrealdays;
    private BigDecimal daysconsumed;
    private BigDecimal varadvplannedperc;
    private BigDecimal efectivityperc;
}
