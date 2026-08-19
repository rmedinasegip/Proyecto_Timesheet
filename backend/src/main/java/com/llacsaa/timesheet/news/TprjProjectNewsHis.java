package com.llacsaa.timesheet.news;

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
@Table(name = "tprj_project_news_his")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectNewsHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqHis;

    private String actiondml;
    private LocalDateTime datechange;
    private Long userchange;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;
    private Long seqNews;

    private LocalDate datenewarrival;
    private String newstypeimpactcat;
    private String newstypeimpact;
    private String descriptionnews;
    private String personreporting;
    private String affectation;
    private String personincharge;
    private String company;
    private String solution;
    private LocalDate datesolution;
    private LocalDate daterealsolution;
    private String newstatuscat;
    private String newstatus;

    private Long usercreate;
    private Long userlastmodify;
    private LocalDateTime datecreate;
    private LocalDateTime datemodify;

    public static TprjProjectNewsHis snapshotOf(TprjProjectNews n, String actiondml, long userchange) {
        TprjProjectNewsHis h = new TprjProjectNewsHis();
        h.setActiondml(actiondml);
        h.setDatechange(LocalDateTime.now());
        h.setUserchange(userchange);
        h.setCodeinstance(n.getCodeinstance());
        h.setCodecompany(n.getCodecompany());
        h.setSeqproject(n.getSeqproject());
        h.setSeqNews(n.getSeqNews());
        h.setDatenewarrival(n.getDatenewarrival());
        h.setNewstypeimpactcat(n.getNewstypeimpactcat());
        h.setNewstypeimpact(n.getNewstypeimpact());
        h.setDescriptionnews(n.getDescriptionnews());
        h.setPersonreporting(n.getPersonreporting());
        h.setAffectation(n.getAffectation());
        h.setPersonincharge(n.getPersonincharge());
        h.setCompany(n.getCompany());
        h.setSolution(n.getSolution());
        h.setDatesolution(n.getDatesolution());
        h.setDaterealsolution(n.getDaterealsolution());
        h.setNewstatuscat(n.getNewstatuscat());
        h.setNewstatus(n.getNewstatus());
        h.setUsercreate(n.getUsercreate());
        h.setUserlastmodify(n.getUserlastmodify());
        h.setDatecreate(n.getDatecreate());
        h.setDatemodify(n.getDatemodify());
        return h;
    }
}
