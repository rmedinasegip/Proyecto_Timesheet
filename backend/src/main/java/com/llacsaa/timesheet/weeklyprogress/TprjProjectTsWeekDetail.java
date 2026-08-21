package com.llacsaa.timesheet.weeklyprogress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tprj_projectts_week_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTsWeekDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqtsweekdt;

    private String codeinstance;
    private String codecompany;
    private Long seqtsweek;

    private LocalDate datefrom;
    private LocalDate dateto;

    // Nombres explícitos: la estrategia de nombres por defecto no inserta "_"
    // entre un dígito y la mayúscula siguiente (day1Perc -> "day1perc", no
    // "day1_perc" como está la columna real en V7__schedule_weekly_progress.sql).
    @Column(name = "day1_perc")
    private BigDecimal day1Perc;
    @Column(name = "day2_perc")
    private BigDecimal day2Perc;
    @Column(name = "day3_perc")
    private BigDecimal day3Perc;
    @Column(name = "day4_perc")
    private BigDecimal day4Perc;
    @Column(name = "day5_perc")
    private BigDecimal day5Perc;
    @Column(name = "day6_perc")
    private BigDecimal day6Perc;
    @Column(name = "day7_perc")
    private BigDecimal day7Perc;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;
}
