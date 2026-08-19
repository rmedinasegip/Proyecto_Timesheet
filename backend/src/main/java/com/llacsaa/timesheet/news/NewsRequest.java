package com.llacsaa.timesheet.news;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewsRequest {
    private LocalDate datenewarrival;
    private String newstypeimpact;
    private String descriptionnews;
    private String personreporting;
    private String affectation;
    private String personincharge;
    private String company;
    private String solution;
    private LocalDate datesolution;
    private LocalDate daterealsolution;
    private String newstatus;
}
