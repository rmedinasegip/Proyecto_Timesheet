package com.llacsaa.timesheet.risk;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RiskRequest {
    private LocalDate riskdate;
    private String risktypeimpact;
    private String riskdescimpact;
    private String personincharge;
    private String company;
    private String solution;
    private BigDecimal probabilityperc;
    private String riskstatus;
}
