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
@Table(name = "tprj_project_schedulets_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectScheduleTsHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqts;
    private Long seqproject;
    private Long seqschedule;

    private LocalDate statementdate;
    private LocalDate datefrom;
    private LocalDate dateto;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectScheduleTsHis snapshotOf(TprjProjectScheduleTs s, String actiondml, long userchange) {
        TprjProjectScheduleTsHis h = new TprjProjectScheduleTsHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(s.getCodeinstance());
        h.setCodecompany(s.getCodecompany());
        h.setSeqts(s.getSeqts());
        h.setSeqproject(s.getSeqproject());
        h.setSeqschedule(s.getSeqschedule());
        h.setStatementdate(s.getStatementdate());
        h.setDatefrom(s.getDatefrom());
        h.setDateto(s.getDateto());
        h.setUsercreate(s.getUsercreate());
        h.setUserlastmodify(s.getUserlastmodify());
        h.setDatecreate(s.getDatecreate());
        h.setDatemodify(s.getDatemodify());
        return h;
    }
}
