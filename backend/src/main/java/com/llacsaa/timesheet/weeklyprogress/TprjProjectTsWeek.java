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
@Table(name = "tprj_projectts_week")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTsWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqtsweek;

    private String codeinstance;
    private String codecompany;
    private Long seqtimesheets;

    private LocalDate datefrom;
    private LocalDate dateto;

    private String statuscat;
    private String status;
    private Long reviewedby;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
