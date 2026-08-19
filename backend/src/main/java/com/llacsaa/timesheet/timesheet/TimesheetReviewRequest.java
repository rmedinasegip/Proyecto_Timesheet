package com.llacsaa.timesheet.timesheet;

import lombok.Data;

@Data
public class TimesheetReviewRequest {
    /** APR (Aprobado) o REC (Rechazado). */
    private String status;
    private Long reviewedby;
}
