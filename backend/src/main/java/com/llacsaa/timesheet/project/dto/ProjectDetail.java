package com.llacsaa.timesheet.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProjectDetail {
    // Cabecera / Datos Generales
    private Long seq;
    private String contractNumber;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private Long codecustomer;
    private String customerName;
    private LocalDate requestDate;
    private Long codeuserPm;
    private String pmName;

    // Planificación
    private BigDecimal projectDuration;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    // Ejecución Real
    private LocalDate realStartDate;
    private LocalDate realEndDate;
    private String statuscat;
    private String status;
    private String statusName;

    // Avance (solo lectura)
    private LocalDate lastcutoffdate;
    private BigDecimal advexpectedperc;
    private BigDecimal advrealperc;
    private BigDecimal advexpecteddays;
    private BigDecimal advrealdays;
    private BigDecimal daysconsumed;
    private BigDecimal varadvplannedperc;
    private BigDecimal efectivityperc;

    // Auditoría (solo lectura)
    private Long usercreate;
    private String usercreateName;
    private LocalDateTime datecreate;
    private Long userlastmodify;
    private String userlastmodifyName;
    private LocalDateTime datemodify;
}
