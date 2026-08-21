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
@Table(name = "tprj_projectts_week_detail_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTsWeekDetailHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqtsweekdt;
    private Long seqtsweek;

    private LocalDate datefrom;
    private LocalDate dateto;

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

    public static TprjProjectTsWeekDetailHis snapshotOf(TprjProjectTsWeekDetail d, String actiondml, long userchange) {
        TprjProjectTsWeekDetailHis h = new TprjProjectTsWeekDetailHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(d.getCodeinstance());
        h.setCodecompany(d.getCodecompany());
        h.setSeqtsweekdt(d.getSeqtsweekdt());
        h.setSeqtsweek(d.getSeqtsweek());
        h.setDatefrom(d.getDatefrom());
        h.setDateto(d.getDateto());
        h.setDay1Perc(d.getDay1Perc());
        h.setDay2Perc(d.getDay2Perc());
        h.setDay3Perc(d.getDay3Perc());
        h.setDay4Perc(d.getDay4Perc());
        h.setDay5Perc(d.getDay5Perc());
        h.setDay6Perc(d.getDay6Perc());
        h.setDay7Perc(d.getDay7Perc());
        h.setUsercreate(d.getUsercreate());
        h.setUserlastmodify(d.getUserlastmodify());
        h.setDatecreate(d.getDatecreate());
        h.setDatemodify(d.getDatemodify());
        return h;
    }
}
