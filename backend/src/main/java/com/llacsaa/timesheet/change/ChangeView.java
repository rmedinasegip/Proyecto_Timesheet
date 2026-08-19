package com.llacsaa.timesheet.change;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ChangeView {
    private Long seqchange;
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
