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
@Table(name = "tprj_projectts_week_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectTsWeekHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqtsweek;
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

    public static TprjProjectTsWeekHis snapshotOf(TprjProjectTsWeek w, String actiondml, long userchange) {
        TprjProjectTsWeekHis h = new TprjProjectTsWeekHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(w.getCodeinstance());
        h.setCodecompany(w.getCodecompany());
        h.setSeqtsweek(w.getSeqtsweek());
        h.setSeqtimesheets(w.getSeqtimesheets());
        h.setDatefrom(w.getDatefrom());
        h.setDateto(w.getDateto());
        h.setStatuscat(w.getStatuscat());
        h.setStatus(w.getStatus());
        h.setReviewedby(w.getReviewedby());
        h.setUsercreate(w.getUsercreate());
        h.setUserlastmodify(w.getUserlastmodify());
        h.setDatecreate(w.getDatecreate());
        h.setDatemodify(w.getDatemodify());
        return h;
    }
}
