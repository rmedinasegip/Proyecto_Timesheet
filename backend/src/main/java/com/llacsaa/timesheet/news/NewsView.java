package com.llacsaa.timesheet.news;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class NewsView {
    private Long seqNews;
    private LocalDate datenewarrival;
    private String newstypeimpactcat;
    private String newstypeimpact;
    private String newstypeimpactName;
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
    private String newstatusName;
}
