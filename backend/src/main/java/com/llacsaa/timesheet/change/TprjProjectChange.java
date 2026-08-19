package com.llacsaa.timesheet.change;

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
@Table(name = "tprj_project_change")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqchange;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;

    private LocalDate changedate;
    private String phase;
    private String deliverable;
    private String reason;
    private String consequence;
    private String approvedby;
    private String company;
    private BigDecimal daysvariation;
    private LocalDate plannedapplydate;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
