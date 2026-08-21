package com.llacsaa.timesheet.weeklyprogress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_project_schedulets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectScheduleTs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqts;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;
    private Long seqschedule;

    private LocalDate statementdate;
    private LocalDate datefrom;
    private LocalDate dateto;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
