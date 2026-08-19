package com.llacsaa.timesheet.timesheet;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimesheetRequest {
    private Long memberuser;
    private String codecompanyconsultant;
    private Long codecustomer;
    private Long seqproject;
    private String system;
    private String module;
    private String sprint;
    private String incidentref;
    private String activitytype;
    private String activitydesc;
    private LocalDate tsdate;
    private LocalTime starttime;
    private LocalTime endtime;
    private Long seqschedule;
}
