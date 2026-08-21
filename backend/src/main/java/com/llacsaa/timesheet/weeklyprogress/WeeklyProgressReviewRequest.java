package com.llacsaa.timesheet.weeklyprogress;

import lombok.Data;

@Data
public class WeeklyProgressReviewRequest {
    /** APR (Aprobado) o REC (Rechazado). */
    private String status;
}
