package com.llacsaa.timesheet.risk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RiskView {
    private Long seqrisk;
    private LocalDate riskdate;
    private String risktypeimpactcat;
    private String risktypeimpact;
    private String risktypeimpactName;
    private String riskdescimpact;
    private String personincharge;
    private String company;
    private String solution;
    private BigDecimal probabilityperc;
    private String riskstatuscat;
    private String riskstatus;
    private String riskstatusName;
}
