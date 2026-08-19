package com.llacsaa.timesheet.report.dto;

import com.llacsaa.timesheet.change.ChangeView;
import com.llacsaa.timesheet.news.NewsView;
import com.llacsaa.timesheet.risk.RiskView;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectProgressReport {
    // Datos generales
    private Long seqproject;
    private String projectName;
    private String customerName;
    private String statusName;
    private String leaderName;
    private LocalDate startDate;
    private LocalDate reportDate;

    // Situación general
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate realStartDate;
    private LocalDate realEndDate;

    // Días del proyecto por fase (incluye fila TOTAL al final)
    private List<PhaseProgressRow> phases;

    // Hitos
    private List<MilestoneRow> milestones;

    // Manejo de riesgos / novedades / cambios aprobados (filtrados por fecha <= reportDate)
    private List<RiskView> risks;
    private List<NewsView> news;
    private List<ChangeView> changes;
}
