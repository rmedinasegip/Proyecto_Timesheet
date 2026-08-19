package com.llacsaa.timesheet.project.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectSaveRequest {
    private String contractNumber;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private Long codecustomer;
    private Long codeuserPm;
    private LocalDate requestDate;

    private BigDecimal projectDuration;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    private LocalDate realStartDate;
    private LocalDate realEndDate;
    private String statuscat;
    private String status;
}
