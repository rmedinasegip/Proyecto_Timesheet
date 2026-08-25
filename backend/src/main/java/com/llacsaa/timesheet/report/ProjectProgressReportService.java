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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
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
 *   - "Días T.S." (daysConsumedTs) no es una columna almacenada en ninguna de
 *     las dos secciones: se calcula como fecha de corte - una fecha de
 *     inicio (ver {@link #daysConsumedFromCutoff}), no sumando horas de
 *     tprj_project_timesheet como en versiones anteriores de este reporte.
 *     En "Días del proyecto por fase" es fecha de corte - fecha inicio real
 *     del proyecto (tprj_project.realStartDate) — un único valor a nivel de
 *     proyecto, igual en cada fila de fase y en la fila TOTAL (no tendría
 *     sentido sumarlo entre fases). En "Hitos" es fecha de corte - fecha
 *     inicio base del propio hito (schedule.baseStartDate) — cada hito tiene
 *     su propia fecha de inicio, a diferencia de la fase.
 *   - "% Variación avance proyecto" tampoco es una columna almacenada: se
 *     calcula advrealperc - advexpectedperc (varianza entre lo real y lo
 *     esperado).
 *   - "% Var. asig. planif." (varadvplannedperc) tampoco se lee de columna
 *     almacenada (ese campo nunca se alimenta desde ningún módulo) — se
 *     calcula en vivo como (Avance real (d) / Total) x 100, redondeado a 2
 *     decimales; si Total es cero o nulo, se deja en null (el gauge del
 *     frontend lo muestra como "—" en vez de una división inválida).
 *   - "Avance esperado (d)" (advexpecteddays) tampoco se lee de columna
 *     almacenada por el mismo motivo — se calcula en vivo como Contratados
 *     x (% Var. asig. planif. / 100); si este último es null (Total en
 *     cero, ver el punto anterior), también queda null en vez de propagar
 *     una multiplicación inválida.
 *   - "% Efectividad" (efectivityperc) tampoco se lee de columna almacenada
 *     por el mismo motivo — se calcula en vivo como % Avance real / % Var.
 *     asig. planif. (cociente directo, sin reescalar a porcentaje), mismo
 *     criterio de null que el punto anterior cuando el denominador es cero
 *     o nulo.
 *   - Fila TOTAL: los campos de "días" sí se suman entre fases (Suma real de
 *     lo comprometido/consumido). "% Var. asig. planif." se recalcula igual
 *     que en cada fase, a partir de los totales ya sumados de la fila
 *     TOTAL. Ninguna de las demás columnas de % de la fila TOTAL ("% Var.
 *     avance", "% Avance actual", "% Avance real", "% Efectividad") se lee
 *     de tprj_project — esas columnas de cabecera (advexpectedperc/
 *     advrealperc/efectivityperc) pueden seguir sin registrar aunque las
 *     fases sí tengan datos, lo que antes daba 0/"—" incorrectamente: en
 *     vez de eso se toma el promedio ({@link #averagePercent}) del valor
 *     ya calculado en cada fase, ignorando las que dieran null.
 *   - "Hitos": actividades "Hijo" (memberuser no nulo), agrupadas por su
 *     fase padre, con las mismas métricas. A diferencia de las fases (que
 *     agregan/promedian entre sus hijas), acá "% Var. asig. planif." y
 *     "% Efectividad" se calculan directo sobre los propios
 *     advrealdays/basedaystotal/advrealperc del hito — son justamente los
 *     nodos "Hijo" que alimenta Avance Semanal (Fase 6), así que ya traen
 *     dato real sin necesidad de agregación.
 *   - Riesgos/Novedades/Cambios Aprobados se filtran por su propia fecha
 *     <= fecha de corte del reporte (fechaInforme) — la única semántica
 *     razonable de "reporte de avance a una fecha de corte" para listas que
 *     ya tienen fecha propia.
 */
@Service
public class ProjectProgressReportService {

    private final ProjectRepository projectRepository;
    private final ScheduleRepository scheduleRepository;
    private final RiskRepository riskRepository;
    private final NewsRepository newsRepository;
    private final ChangeRepository changeRepository;
    private final NameResolver nameResolver;

    public ProjectProgressReportService(ProjectRepository projectRepository,
                                         ScheduleRepository scheduleRepository,
                                         RiskRepository riskRepository,
                                         NewsRepository newsRepository,
                                         ChangeRepository changeRepository,
                                         NameResolver nameResolver) {
        this.projectRepository = projectRepository;
        this.scheduleRepository = scheduleRepository;
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

        BigDecimal daysConsumedTs = daysConsumedFromCutoff(project.getRealStartDate(), reportDate);

        List<PhaseProgressRow> phases = phaseRows.stream()
                .map(phase -> toPhaseRow(phase, milestoneRows, daysConsumedTs))
                .collect(Collectors.toList());
        phases.add(totalRow(phases, daysConsumedTs));

        List<MilestoneRow> milestones = milestoneRows.stream()
                .sorted(Comparator.comparing(TprjProjectSchedule::getSeqschedule))
                .map(m -> toMilestoneRow(m, phaseRows, reportDate))
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
                                         BigDecimal daysConsumedTs) {
        List<TprjProjectSchedule> children = milestoneRows.stream()
                .filter(m -> phase.getSeqschedule().equals(m.getSeqscheduleparent()))
                .collect(Collectors.toList());

        BigDecimal contractedDays = ownOrSumChildren(phase.getBasedays(), children, TprjProjectSchedule::getBasedays);
        BigDecimal addendumDays = ownOrSumChildren(phase.getBaseadicional(), children, TprjProjectSchedule::getBaseadicional);
        BigDecimal totalProjectDays = ownOrSumChildren(phase.getBasedaystotal(), children, TprjProjectSchedule::getBasedaystotal);
        BigDecimal realAdvanceDays = ownOrSumChildren(phase.getAdvrealdays(), children, TprjProjectSchedule::getAdvrealdays);
        BigDecimal expectedPerc = ownOrAvgChildren(phase.getAdvexpectedperc(), children, TprjProjectSchedule::getAdvexpectedperc);
        BigDecimal realPerc = ownOrAvgChildren(phase.getAdvrealperc(), children, TprjProjectSchedule::getAdvrealperc);

        BigDecimal daysToInvest = totalProjectDays.subtract(realAdvanceDays);
        BigDecimal advanceVariation = realPerc.subtract(expectedPerc);

        BigDecimal balanceDaysTs = daysConsumedTs != null ? totalProjectDays.subtract(daysConsumedTs) : null;

        BigDecimal plannedAssignmentVariationPerc = plannedAssignmentVariation(realAdvanceDays, totalProjectDays);
        BigDecimal effectivenessPerc = effectiveness(realPerc, plannedAssignmentVariationPerc);
        BigDecimal expectedAdvanceDays = expectedAdvanceDays(contractedDays, plannedAssignmentVariationPerc);

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

    /**
     * % Var. asig. planif. = (Avance real (d) / Total) x 100. Se recalcula
     * en cada lectura del reporte a partir de las columnas de días ya
     * mostradas en la misma fila, para que quede consistente si alguna de
     * las dos cambia. Total en cero/nulo no es un caso de error de negocio
     * (una fase sin días asignados todavía) — se devuelve null para que el
     * gauge del frontend lo pinte como "—" en vez de dividir por cero.
     */
    private BigDecimal plannedAssignmentVariation(BigDecimal realAdvanceDays, BigDecimal totalProjectDays) {
        if (totalProjectDays == null || totalProjectDays.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return nz(realAdvanceDays)
                .multiply(BigDecimal.valueOf(100))
                .divide(totalProjectDays, 2, RoundingMode.HALF_UP);
    }

    /**
     * % Efectividad = % Avance real / % Var. asig. planif. — cociente
     * directo, sin reescalar a porcentaje (ajustado a pedido explícito,
     * distinto del criterio de {@link #plannedAssignmentVariation}).
     * Denominador nulo o cero (fase sin días asignados, ver
     * {@link #plannedAssignmentVariation}) devuelve null en vez de dividir
     * por cero.
     */
    private BigDecimal effectiveness(BigDecimal realPerc, BigDecimal plannedAssignmentVariationPerc) {
        if (plannedAssignmentVariationPerc == null || plannedAssignmentVariationPerc.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return nz(realPerc).divide(plannedAssignmentVariationPerc, 2, RoundingMode.HALF_UP);
    }

    /**
     * Avance esperado (d) = Contratados x (% Var. asig. planif. / 100).
     * Antes se leía de schedule.advexpecteddays, otra columna que ningún
     * módulo alimenta. Si Contratados o % Var. asig. planif. vienen null
     * (este último ya devuelve null cuando Total es cero, ver
     * {@link #plannedAssignmentVariation}), el resultado también es null en
     * vez de una multiplicación inválida; Contratados en cero es un caso
     * normal (fase sin días contratados) y simplemente da 0, sin necesidad
     * de un guard aparte.
     */
    private BigDecimal expectedAdvanceDays(BigDecimal contractedDays, BigDecimal plannedAssignmentVariationPerc) {
        if (contractedDays == null || plannedAssignmentVariationPerc == null) {
            return null;
        }
        return contractedDays
                .multiply(plannedAssignmentVariationPerc)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Días T.S. (sección "Días del proyecto por fase") = Fecha de corte -
     * Fecha inicio real del proyecto. Es un valor a nivel de proyecto (no
     * por fase, a diferencia de Contratados/Total/etc.), así que se calcula
     * una sola vez en {@link #getReport} y se reutiliza igual en cada fila
     * de fase y en la fila TOTAL — no tiene sentido sumarlo entre fases,
     * daría un múltiplo del valor real. Reemplaza el cálculo anterior
     * (suma de tprj_project_timesheet.hoursconsumed / 8), que sigue
     * vigente sin cambios para la sección "Hitos" (ver
     * {@link #toMilestoneRow}/{@link #sumDaysConsumed}), fuera del alcance
     * de este ajuste. Si el proyecto no tiene Fecha inicio real registrada,
     * devuelve null en vez de una resta inválida (el gauge/celda del
     * frontend ya lo muestra como "—").
     */
    private BigDecimal daysConsumedFromCutoff(LocalDate realStartDate, LocalDate reportDate) {
        if (realStartDate == null || reportDate == null) {
            return null;
        }
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(realStartDate, reportDate));
    }

    /**
     * Promedia una columna de % ya calculada en cada fila de fase, para la
     * fila TOTAL — en vez de leerla de una columna de cabecera de
     * tprj_project que puede seguir sin registrar aunque las fases sí
     * tengan datos (ver usos en {@link #totalRow}, antes daba 0/"—"
     * incorrectamente en ese caso). Filas con el valor en null (sin datos,
     * o denominador en cero en el cálculo de esa fase) se excluyen del
     * promedio en vez de contar como cero; si ninguna fase tiene un valor
     * calculable, el TOTAL también queda en null.
     */
    private BigDecimal averagePercent(List<PhaseProgressRow> phaseRows,
                                       java.util.function.Function<PhaseProgressRow, BigDecimal> extractor) {
        List<BigDecimal> values = phaseRows.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
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

    private PhaseProgressRow totalRow(List<PhaseProgressRow> phaseRows, BigDecimal daysConsumedTs) {
        BigDecimal totalContracted = sum(phaseRows, PhaseProgressRow::getContractedDays);
        BigDecimal totalAddendum = sum(phaseRows, PhaseProgressRow::getAddendumDays);
        BigDecimal totalProjectDays = sum(phaseRows, PhaseProgressRow::getTotalProjectDays);
        BigDecimal totalRealDays = sum(phaseRows, PhaseProgressRow::getRealAdvanceDays);
        BigDecimal totalDaysToInvest = sum(phaseRows, PhaseProgressRow::getDaysToInvest);
        BigDecimal totalBalanceDaysTs = daysConsumedTs != null ? totalProjectDays.subtract(daysConsumedTs) : null;

        BigDecimal plannedAssignmentVariationPerc = plannedAssignmentVariation(totalRealDays, totalProjectDays);
        BigDecimal totalExpectedDays = expectedAdvanceDays(totalContracted, plannedAssignmentVariationPerc);
        BigDecimal averageAdvanceVariationPerc = averagePercent(phaseRows, PhaseProgressRow::getAdvanceVariationPerc);
        BigDecimal averageCurrentAdvancePerc = averagePercent(phaseRows, PhaseProgressRow::getCurrentAdvancePerc);
        BigDecimal averageRealAdvancePerc = averagePercent(phaseRows, PhaseProgressRow::getRealAdvancePerc);

        return new PhaseProgressRow(
                null,
                "TOTAL",
                totalContracted,
                totalAddendum,
                totalProjectDays,
                totalExpectedDays,
                totalRealDays,
                totalDaysToInvest,
                averageAdvanceVariationPerc,
                daysConsumedTs,
                totalBalanceDaysTs,
                plannedAssignmentVariationPerc,
                averageCurrentAdvancePerc,
                averageRealAdvancePerc,
                averagePercent(phaseRows, PhaseProgressRow::getEffectivenessPerc),
                true
        );
    }

    private MilestoneRow toMilestoneRow(TprjProjectSchedule milestone, List<TprjProjectSchedule> phaseRows,
                                         LocalDate reportDate) {
        String parentDescription = phaseRows.stream()
                .filter(p -> p.getSeqschedule().equals(milestone.getSeqscheduleparent()))
                .findFirst()
                .map(TprjProjectSchedule::getShortactivitydesc)
                .orElse(null);
        BigDecimal daysConsumedTs = daysConsumedFromCutoff(milestone.getBaseStartDate(), reportDate);

        BigDecimal plannedAssignmentVariationPerc = plannedAssignmentVariation(milestone.getAdvrealdays(), milestone.getBasedaystotal());
        BigDecimal effectivenessPerc = effectiveness(milestone.getAdvrealperc(), plannedAssignmentVariationPerc);

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
                plannedAssignmentVariationPerc,
                milestone.getAdvrealperc(),
                effectivenessPerc,
                daysConsumedTs,
                milestone.getBaseStartDate(),
                milestone.getBaseEndDate()
        );
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
