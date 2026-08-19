package com.llacsaa.timesheet.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ProjectListItem {
    private Long seq;
    private LocalDate requestDate;
    private String projectCode;
    private String projectName;
    private String customerName;
    private Long codeuserPm;
    private String pmName;
    private Long baseDurationDays;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private Long plannedDurationDays;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String statusName;
}
