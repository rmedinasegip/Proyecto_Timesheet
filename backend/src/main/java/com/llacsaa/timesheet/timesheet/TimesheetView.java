package com.llacsaa.timesheet.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class TimesheetView {
    private Long seqts;
    private Long memberuser;
    private String memberName;
    private String codecompanyconsultant;
    private String codecompanyconsultantName;
    private Long codecustomer;
    private String customerName;
    private Long seqproject;
    private String projectName;
    private String system;
    private String systemName;
    private String module;
    private String moduleName;
    private String sprint;
    private String incidentref;
    private String activitytype;
    private String activitytypeName;
    private String activitydesc;
    private LocalDate tsdate;
    private LocalTime starttime;
    private LocalTime endtime;
    private BigDecimal hoursconsumed;
    private String status;
    private String statusName;
    private Long reviewedby;
    private String reviewedbyName;
    private Long seqschedule;
}
