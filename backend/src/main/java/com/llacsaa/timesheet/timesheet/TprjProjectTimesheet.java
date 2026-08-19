package com.llacsaa.timesheet.timesheet;

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
import java.time.LocalTime;

@Entity
@Table(name = "tprj_project_timesheet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTimesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqts;

    private String codeinstance;
    private String codecompany;

    private Long memberuser;
    private String codecompanyconsultant;
    private Long codecustomer;
    private Long seqproject;

    private String systemcat;
    private String system;
    private String modulecat;
    private String module;
    private String sprint;
    private String incidentref;
    private String activitytypecat;
    private String activitytype;
    private String activitydesc;

    private LocalDate tsdate;
    private LocalTime starttime;
    private LocalTime endtime;
    private BigDecimal hoursconsumed;

    private String statuscat;
    private String status;
    private Long reviewedby;
    private Long seqschedule;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
