package com.llacsaa.timesheet.risk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_project_risk")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqrisk;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;

    private LocalDate riskdate;
    private String risktypeimpactcat;
    private String risktypeimpact;
    private String riskdescimpact;
    private String personincharge;
    private String company;
    private String solution;
    private BigDecimal probabilityperc;
    private String riskstatuscat;
    private String riskstatus;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
