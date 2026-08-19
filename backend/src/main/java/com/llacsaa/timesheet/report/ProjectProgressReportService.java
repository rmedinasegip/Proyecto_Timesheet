package com.llacsaa.timesheet.report;

import com.llacsaa.timesheet.change.ChangeRepository;
import com.llacsaa.timesheet.change.ChangeView;
import com.llacsaa.timesheet.common.NameResolver;
import com.llacsaa.timesheet.news.NewsRepository;
import com.llacsaa.timesheet.news.NewsView;
import com.llacsaa.timesheet.news.TprjProjectNews;
import com.llacsaa.timesheet.project.ProjectRepository;
import com.llacsaa.timesheet.project.TprjProject;
import com.llacsaa.timesheet.report.dto.MilestoneRow;
import com.llacsaa.timesheet.report.dto.PhaseProgressRow;
import com.llacsaa.timesheet.report.dto.ProjectProgressReport;
import com.llacsaa.timesheet.risk.RiskRepository;
import com.llacsaa.timesheet.risk.RiskView;
import com.llacsaa.timesheet.risk.TprjProjectRisk;
import com.llacsaa.timesheet.schedule.ScheduleRepository;
import com.llacsaa.timesheet.schedule.TprjProjectSchedule;
import com.llacsaa.timesheet.timesheet.TimesheetRepository;
import com.llacsaa.timesheet.timesheet.TprjProjectTimesheet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reporte de "Avance de Proyectos" (Fase 4), equivalente a la hoja "Detalle"
 * de AD-RE-04_TIME_SHEET_2026.xlsx. Sin documento CRUD dedicado — reconstruye
 * las columnas del Excel a partir de los datos ya modelados en Fases 1-3, con
 * las siguientes interpretaciones (documentadas también en docs/erd.md):
 *
 *   - "Días del proyecto por fase": una fila por actividad "Padre" de
 *     tprj_project_schedule (memberuser nulo = fase). La regla de negocio
 *     de Fase 2 (ver ScheduleService) deja las columnas de días/% de una
 *     fila "Padre" siempre en null — solo sus hijas ("Hijo") las tienen
 *     editables. Por eso cada fila de fase agrega (suma para días,
 *     promedio para %) las columnas de sus actividades "Hijo" cuando la
 *     propia fila Padre no trae el valor: leer directo la columna del
 *     Padre (siempre null en la práctica) dejaría esta sección del
 *     reporte vacía para cualquier proyecto real.
 *   - "Días Utilizados (según T.S.)" no es una columna almacenada: se agrega
 *     en vivo sumando tprj_project_timesheet.hoursconsumed de los registros
 *     vinculados a esa fase (su propio seqschedule + el de sus hijos) y
 *     dividiendo entre HOURS_PER_DAY (8, jornada estándar — no hay
 *     documento que fije este valor, se asume igual que se asumieron los
 *     campos de Fase 3 sin documento CRUD dedicado).
 *   - "% Variación avance proyecto" tampoco es una columna almacenada: se
 *     calcula advrealperc - advexpectedperc (varianza entre lo real y lo
 *     esperado), distinto de "% Variación asignación planificada" que sí
 *     tiene su propia columna (varadvplannedperc).
 *   - Fila TOTAL: los campos de "días" sí se suman entre fases (Suma real de
 *     lo comprometido/consumido); los campos de "%" se toman directo de
 *     tprj_project (el snapshot de avance a nivel de proyecto registrado vía
 *     "Registrar avance" — ver ProjectService#registerProgress), no un
 *     promedio de las fases, porque esos campos existen justamente para
 *     representar el avance global tal como lo evalúa el PM.
 *   - "Hitos": actividades "Hijo" (memberuser no nulo), agrupadas por su
 *     fase padre, con las mismas métricas.
 *   - Riesgos/Novedades/Cambios Aprobados se filtran por su propia fecha
 *     <= fecha de corte del reporte (fechaInforme) — la única semántica
 *     razonable de "reporte de avance a una fecha de corte" para listas que
 *     ya tienen fecha propia.
 */
@Service
public class ProjectProgressReportService {

    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(8);

    private final ProjectRepository projectRepository;
    private final ScheduleRepository scheduleRepository;
    private final TimesheetRepository timesheetRepository;
    private final RiskRepository riskRepository;
    private final NewsRepository newsRepository;
    private final ChangeRepository changeRepository;
    private final NameResolver nameResolver;

    public ProjectProgressReportService(ProjectRepository projectRepository,
                                         ScheduleRepository scheduleRepository,
                                         TimesheetRepository timesheetRepository,
                                         RiskRepository riskRepository,
                                         NewsRepository newsRepository,
                                         ChangeRepository changeRepository,
                                         NameResolver nameResolver) {
        this.projectRepository = projectRepository;
        this.scheduleRepository = scheduleRepository;
        this.timesheetRepository = timesheetRepository;
        this.riskRepository = riskRepository;
        this.newsRepository = newsRepository;
        this.changeRepository = changeRepository;
        this.nameResolver = nameResolver;
    }

    public ProjectProgressReport getReport(Long seqproject, LocalDate cutoffDate) {
        TprjProject project = projectRepository.findById(seqproject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado: " + seqproject));
        LocalDate reportDate = cutoffDate != null ? cutoffDate : LocalDate.now();

        List<TprjProjectSchedule> schedule = scheduleRepository.findBySeqprojectOrderBySeqschedule(seqproject);
        List<TprjProjectSchedule> phaseRows = schedule.stream()
                .filter(s -> s.getMemberuser() == null)
                .collect(Collectors.toList());
        List<TprjProjectSchedule> milestoneRows = schedule.stream()
                .filter(s -> s.getMemberuser() != null)
                .collect(Collectors.toList());

        List<TprjProjectTimesheet> timesheets = timesheetRepository.findBySeqproject(seqproject);
        Map<Long, List<TprjProjectTimesheet>> timesheetsBySchedule = timesheets.stream()
                .filter(t -> t.getSeqschedule() != null)
                .collect(Collectors.groupingBy(TprjProjectTimesheet::getSeqschedule));

        List<PhaseProgressRow> phases = phaseRows.stream()
                .map(phase -> toPhaseRow(phase, milestoneRows, timesheetsBySchedule))
                .collect(Collectors.toList());
        phases.add(totalRow(project, phases));

        List<MilestoneRow> milestones = milestoneRows.stream()
                .sorted(Comparator.comparing(TprjProjectSchedule::getSeqschedule))
                .map(m -> toMilestoneRow(m, phaseRows, timesheetsBySchedule))
                .collect(Collectors.toList());

        List<RiskView> risks = riskRepository.findBySeqprojectOrderByRiskdateDesc(seqproject).stream()
                .filter(r -> !r.getRiskdate().isAfter(reportDate))
                .map(this::toRiskView)
                .collect(Collectors.toList());

        List<NewsView> news = newsRepository.findBySeqprojectOrderByDatenewarrivalDesc(seqproject).stream()
                .filter(n -> !n.getDatenewarrival().isAfter(reportDate))
                .map(this::toNewsView)
                .collect(Collectors.toList());

        List<ChangeView> changes = changeRepository.findBySeqprojectOrderByChangedateDesc(seqproject).stream()
                .filter(c -> !c.getChangedate().isAfter(reportDate))
                .map(this::toChangeView)
                .collect(Collectors.toList());

        return new ProjectProgressReport(
                project.getSeq(),
                project.getProjectName(),
                nameResolver.customerName(project.getCodecustomer()),
                nameResolver.catalogItemName(project.getStatuscat(), project.getStatus()),
                nameResolver.userName(project.getCodeuserPm()),
                project.getBaseStartDate(),
                reportDate,
                project.getPlannedStartDate(),
                project.getPlannedEndDate(),
                project.getRealStartDate(),
                project.getRealEndDate(),
                phases,
                milestones,
                risks,
                news,
                changes
        );
    }

    private PhaseProgressRow toPhaseRow(TprjProjectSchedule phase, List<TprjProjectSchedule> milestoneRows,
                                         Map<Long, List<TprjProjectTimesheet>> timesheetsBySchedule) {
        List<TprjProjectSchedule> children = milestoneRows.stream()
                .filter(m -> phase.getSeqschedule().equals(m.getSeqscheduleparent()))
                .collect(Collectors.toList());
        List<Long> childSeqs = children.stream().map(TprjProjectSchedule::getSeqschedule).collect(Collectors.toList());

        BigDecimal contractedDays = ownOrSumChildren(phase.getBasedays(), children, TprjProjectSchedule::getBasedays);
        BigDecimal addendumDays = ownOrSumChildren(phase.getBaseadicional(), children, TprjProjectSchedule::getBaseadicional);
        BigDecimal totalProjectDays = ownOrSumChildren(phase.getBasedaystotal(), children, TprjProjectSchedule::getBasedaystotal);
        BigDecimal expectedAdvanceDays = ownOrSumChildren(phase.getAdvexpecteddays(), children, TprjProjectSchedule::getAdvexpecteddays);
        BigDecimal realAdvanceDays = ownOrSumChildren(phase.getAdvrealdays(), children, TprjProjectSchedule::getAdvrealdays);
        BigDecimal expectedPerc = ownOrAvgChildren(phase.getAdvexpectedperc(), children, TprjProjectSchedule::getAdvexpectedperc);
        BigDecimal realPerc = ownOrAvgChildren(phase.getAdvrealperc(), children, TprjProjectSchedule::getAdvrealperc);
        BigDecimal plannedAssignmentVariationPerc = ownOrAvgChildren(phase.getVaradvplannedperc(), children, TprjProjectSchedule::getVaradvplannedperc);
        BigDecimal effectivenessPerc = ownOrAvgChildren(phase.getEfectivityperc(), children, TprjProjectSchedule::getEfectivityperc);

        BigDecimal daysToInvest = totalProjectDays.subtract(realAdvanceDays);
        BigDecimal advanceVariation = realPerc.subtract(expectedPerc);

        BigDecimal daysConsumedTs = sumDaysConsumed(timesheetsBySchedule, phase.getSeqschedule(), childSeqs);
        BigDecimal balanceDaysTs = totalProjectDays.subtract(daysConsumedTs);

        return new PhaseProgressRow(
                phase.getSeqschedule(),
                phase.getShortactivitydesc(),
                contractedDays,
                addendumDays,
                totalProjectDays,
                expectedAdvanceDays,
                realAdvanceDays,
                daysToInvest,
                advanceVariation,
                daysConsumedTs,
                balanceDaysTs,
                plannedAssignmentVariationPerc,
                expectedPerc,
                realPerc,
                effectivenessPerc,
                false
        );
    }

    /** Si la fila "Padre" trae el valor, se usa; si no (el caso normal, ver ScheduleService), se suma entre sus hijas. */
    private BigDecimal ownOrSumChildren(BigDecimal ownValue, List<TprjProjectSchedule> children,
                                         java.util.function.Function<TprjProjectSchedule, BigDecimal> extractor) {
        if (ownValue != null) {
            return ownValue;
        }
        return children.stream().map(extractor).map(this::nz).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Misma idea que {@link #ownOrSumChildren} pero promediando — para columnas de %, no de días. */
    private BigDecimal ownOrAvgChildren(BigDecimal ownValue, List<TprjProjectSchedule> children,
                                         java.util.function.Function<TprjProjectSchedule, BigDecimal> extractor) {
        if (ownValue != null) {
            return ownValue;
        }
        List<BigDecimal> values = children.stream().map(extractor).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private PhaseProgressRow totalRow(TprjProject project, List<PhaseProgressRow> phaseRows) {
        BigDecimal totalContracted = sum(phaseRows, PhaseProgressRow::getContractedDays);
        BigDecimal totalAddendum = sum(phaseRows, PhaseProgressRow::getAddendumDays);
        BigDecimal totalProjectDays = sum(phaseRows, PhaseProgressRow::getTotalProjectDays);
        BigDecimal totalExpectedDays = sum(phaseRows, PhaseProgressRow::getExpectedAdvanceDays);
        BigDecimal totalRealDays = sum(phaseRows, PhaseProgressRow::getRealAdvanceDays);
        BigDecimal totalDaysToInvest = sum(phaseRows, PhaseProgressRow::getDaysToInvest);
        BigDecimal totalDaysConsumedTs = sum(phaseRows, PhaseProgressRow::getDaysConsumedTs);
        BigDecimal totalBalanceDaysTs = sum(phaseRows, PhaseProgressRow::getBalanceDaysTs);

        BigDecimal expectedPerc = nz(project.getAdvexpectedperc());
        BigDecimal realPerc = nz(project.getAdvrealperc());
        BigDecimal advanceVariation = realPerc.subtract(expectedPerc);

        return new PhaseProgressRow(
                null,
                "TOTAL",
                totalContracted,
                totalAddendum,
                totalProjectDays,
                totalExpectedDays,
                totalRealDays,
                totalDaysToInvest,
                advanceVariation,
                totalDaysConsumedTs,
                totalBalanceDaysTs,
                project.getVaradvplannedperc(),
                project.getAdvexpectedperc(),
                project.getAdvrealperc(),
                project.getEfectivityperc(),
                true
        );
    }

    private MilestoneRow toMilestoneRow(TprjProjectSchedule milestone, List<TprjProjectSchedule> phaseRows,
                                         Map<Long, List<TprjProjectTimesheet>> timesheetsBySchedule) {
        String parentDescription = phaseRows.stream()
                .filter(p -> p.getSeqschedule().equals(milestone.getSeqscheduleparent()))
                .findFirst()
                .map(TprjProjectSchedule::getShortactivitydesc)
                .orElse(null);
        BigDecimal daysConsumedTs = sumDaysConsumed(timesheetsBySchedule, milestone.getSeqschedule(), List.of());

        return new MilestoneRow(
                milestone.getSeqschedule(),
                milestone.getSeqscheduleparent(),
                parentDescription,
                milestone.getShortactivitydesc(),
                nameResolver.userName(milestone.getMemberuser()),
                milestone.getBasedays(),
                milestone.getBaseadicional(),
                milestone.getBasedaystotal(),
                milestone.getAdvexpecteddays(),
                milestone.getAdvrealdays(),
                milestone.getVaradvplannedperc(),
                milestone.getAdvrealperc(),
                milestone.getEfectivityperc(),
                daysConsumedTs,
                milestone.getBaseStartDate(),
                milestone.getBaseEndDate()
        );
    }

    private BigDecimal sumDaysConsumed(Map<Long, List<TprjProjectTimesheet>> timesheetsBySchedule,
                                        Long seqschedule, List<Long> childSeqs) {
        BigDecimal totalHours = BigDecimal.ZERO;
        for (Long seq : concat(seqschedule, childSeqs)) {
            for (TprjProjectTimesheet t : timesheetsBySchedule.getOrDefault(seq, List.of())) {
                totalHours = totalHours.add(nz(t.getHoursconsumed()));
            }
        }
        return totalHours.divide(HOURS_PER_DAY, 2, RoundingMode.HALF_UP);
    }

    private List<Long> concat(Long seq, List<Long> others) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(seq), others.stream())
                .collect(Collectors.toList());
    }

    private BigDecimal sum(List<PhaseProgressRow> rows, java.util.function.Function<PhaseProgressRow, BigDecimal> extractor) {
        return rows.stream().map(extractor).map(this::nz).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private RiskView toRiskView(TprjProjectRisk r) {
        return new RiskView(
                r.getSeqrisk(), r.getRiskdate(), r.getRisktypeimpactcat(), r.getRisktypeimpact(),
                nameResolver.catalogItemName(r.getRisktypeimpactcat(), r.getRisktypeimpact()),
                r.getRiskdescimpact(), r.getPersonincharge(), r.getCompany(), r.getSolution(),
                r.getProbabilityperc(), r.getRiskstatuscat(), r.getRiskstatus(),
                nameResolver.catalogItemName(r.getRiskstatuscat(), r.getRiskstatus())
        );
    }

    private NewsView toNewsView(TprjProjectNews n) {
        return new NewsView(
                n.getSeqNews(), n.getDatenewarrival(), n.getNewstypeimpactcat(), n.getNewstypeimpact(),
                nameResolver.catalogItemName(n.getNewstypeimpactcat(), n.getNewstypeimpact()),
                n.getDescriptionnews(), n.getPersonreporting(), n.getAffectation(), n.getPersonincharge(),
                n.getCompany(), n.getSolution(), n.getDatesolution(), n.getDaterealsolution(),
                n.getNewstatuscat(), n.getNewstatus(), nameResolver.catalogItemName(n.getNewstatuscat(), n.getNewstatus())
        );
    }

    private ChangeView toChangeView(com.llacsaa.timesheet.change.TprjProjectChange c) {
        return new ChangeView(
                c.getSeqchange(), c.getChangedate(), c.getPhase(), c.getDeliverable(), c.getReason(),
                c.getConsequence(), c.getApprovedby(), c.getCompany(), c.getDaysvariation(), c.getPlannedapplydate()
        );
    }
}
