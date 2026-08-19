package com.llacsaa.timesheet.change;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChangeRequest {
    private LocalDate changedate;
    private String phase;
    private String deliverable;
    private String reason;
    private String consequence;
    private String approvedby;
    private String company;
    private BigDecimal daysvariation;
    private LocalDate plannedapplydate;
}
