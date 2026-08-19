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
@Table(name = "tprj_project_news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TprjProjectNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqNews;

    private String codeinstance;
    private String codecompany;
    private Long seqproject;

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
}
