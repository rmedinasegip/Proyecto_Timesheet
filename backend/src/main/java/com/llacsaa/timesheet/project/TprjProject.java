package com.llacsaa.timesheet.project;

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
@Table(name = "tprj_project")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private String codeinstance;
    private String codecompany;

    private String contractNumber;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private BigDecimal projectDuration;

    private Long codecustomer;
    private Long codeuserPm;

    private LocalDate requestDate;
    private LocalDate baseStartDate;
    private LocalDate baseEndDate;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate realStartDate;
    private LocalDate realEndDate;

    private String statuscat;
    private String status;

    private LocalDate lastcutoffdate;
    private BigDecimal advexpectedperc;
    private BigDecimal advrealperc;
    private BigDecimal advexpecteddays;
    private BigDecimal advrealdays;
    private BigDecimal daysconsumed;
    private BigDecimal varadvplannedperc;
    private BigDecimal efectivityperc;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
