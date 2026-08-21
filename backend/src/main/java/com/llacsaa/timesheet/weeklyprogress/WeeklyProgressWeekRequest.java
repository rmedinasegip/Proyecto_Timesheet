package com.llacsaa.timesheet.weeklyprogress;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WeeklyProgressWeekRequest {
    private Long seqschedule;
    /** Cualquier fecha dentro de la semana a registrar; el servidor la normaliza al lunes. */
    private LocalDate weekStart;

    private BigDecimal day1Perc;
    private BigDecimal day2Perc;
    private BigDecimal day3Perc;
    private BigDecimal day4Perc;
    private BigDecimal day5Perc;
    private BigDecimal day6Perc;
    private BigDecimal day7Perc;
}
