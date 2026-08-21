package com.llacsaa.timesheet.weeklyprogress;

import com.llacsaa.timesheet.auth.AuthContext;
import com.llacsaa.timesheet.common.NameResolver;
import com.llacsaa.timesheet.project.ProjectRepository;
import com.llacsaa.timesheet.project.TprjProject;
import com.llacsaa.timesheet.schedule.ScheduleHisRepository;
import com.llacsaa.timesheet.schedule.ScheduleRepository;
import com.llacsaa.timesheet.schedule.TprjProjectSchedule;
import com.llacsaa.timesheet.schedule.TprjProjectScheduleHis;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;

/**
 * Fase 6 — "Especificación de Proyecto_Control_Proyectos_TimeSheets_2.docx":
 * cada consultor registra, semana a semana, el % de avance de las
 * actividades ("Hijo", memberuser no nulo) de su Schedule que tiene
 * asignadas.
 *
 * Cadena tprj_project_schedule -&gt; tprj_project_schedulets (cabecera de
 * seguimiento, 1 por actividad trackeada) -&gt; tprj_projectts_week (1 por
 * semana) -&gt; tprj_projectts_week_detail (1:1 con la semana, día1-7 =
 * lunes-domingo). day1_perc..day7_perc son un snapshot acumulado por día
 * ("% completado a esa fecha", no un aporte incremental) — al guardar una
 * semana, el último día no nulo se propaga como nuevo
 * tprj_project_schedule.advrealperc/advrealdays/datestamentday, historizado
 * con el mismo patrón que {@code ScheduleService}.
 *
 * Acceso: autoservicio estricto por memberuser — si el usuario actual no es
 * autorizador (AUT), el filtro de consultor se fuerza a
 * {@code AuthContext.currentUserCode()} sin importar qué mande el cliente.
 * Guardar una semana (day1..7) exige además que
 * {@code schedule.memberuser == AuthContext.currentUserCode()} — ni
 * siquiera un AUT puede escribir por otro, solo revisar
 * (ver {@link #review}).
 */
@Service
public class WeeklyProgressService {

    public static final String STATUSCAT = "PRJ_TSWEEKSTATUSCAT";
    private static final String DEFAULT_STATUS = "REG";

    private final ScheduleRepository scheduleRepository;
    private final ScheduleHisRepository scheduleHisRepository;
    private final ScheduleTsRepository scheduleTsRepository;
    private final ScheduleTsHisRepository scheduleTsHisRepository;
    private final TsWeekRepository tsWeekRepository;
    private final TsWeekHisRepository tsWeekHisRepository;
    private final TsWeekDetailRepository tsWeekDetailRepository;
    private final TsWeekDetailHisRepository tsWeekDetailHisRepository;
    private final ProjectRepository projectRepository;
    private final NameResolver nameResolver;

    public WeeklyProgressService(ScheduleRepository scheduleRepository,
                                  ScheduleHisRepository scheduleHisRepository,
                                  ScheduleTsRepository scheduleTsRepository,
                                  ScheduleTsHisRepository scheduleTsHisRepository,
                                  TsWeekRepository tsWeekRepository,
                                  TsWeekHisRepository tsWeekHisRepository,
                                  TsWeekDetailRepository tsWeekDetailRepository,
                                  TsWeekDetailHisRepository tsWeekDetailHisRepository,
                                  ProjectRepository projectRepository,
                                  NameResolver nameResolver) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleHisRepository = scheduleHisRepository;
        this.scheduleTsRepository = scheduleTsRepository;
        this.scheduleTsHisRepository = scheduleTsHisRepository;
        this.tsWeekRepository = tsWeekRepository;
        this.tsWeekHisRepository = tsWeekHisRepository;
        this.tsWeekDetailRepository = tsWeekDetailRepository;
        this.tsWeekDetailHisRepository = tsWeekDetailHisRepository;
        this.projectRepository = projectRepository;
        this.nameResolver = nameResolver;
    }

    public List<WeeklyProgressActivityView> listActivities(LocalDate weekStartParam, Long seqprojectFilter,
                                                             Long memberuserFilter, String statusFilter) {
        LocalDate monday = normalizeToMonday(weekStartParam != null ? weekStartParam : LocalDate.now());

        Long effectiveMemberuser;
        if (AuthContext.isAuthorizer()) {
            effectiveMemberuser = memberuserFilter;
        } else {
            effectiveMemberuser = AuthContext.currentUserCode();
        }

        List<TprjProjectSchedule> schedules = effectiveMemberuser != null
                ? scheduleRepository.findByMemberuser(effectiveMemberuser)
                : scheduleRepository.findByMemberuserIsNotNull();

        if (seqprojectFilter != null) {
            schedules = schedules.stream()
                    .filter(s -> seqprojectFilter.equals(s.getSeqproject()))
                    .collect(Collectors.toList());
        }

        if (schedules.isEmpty()) {
            return List.of();
        }

        List<Long> seqscheduleList = schedules.stream().map(TprjProjectSchedule::getSeqschedule).collect(Collectors.toList());
        Map<Long, TprjProjectScheduleTs> scheduletsBySeqschedule = scheduleTsRepository.findBySeqscheduleIn(seqscheduleList)
                .stream().collect(Collectors.toMap(TprjProjectScheduleTs::getSeqschedule, ts -> ts));

        List<Long> seqtsList = scheduletsBySeqschedule.values().stream()
                .map(TprjProjectScheduleTs::getSeqts).collect(Collectors.toList());
        Map<Long, TprjProjectTsWeek> weekBySeqts = seqtsList.isEmpty() ? Map.of()
                : tsWeekRepository.findBySeqtimesheetsInAndDatefrom(seqtsList, monday)
                        .stream().collect(Collectors.toMap(TprjProjectTsWeek::getSeqtimesheets, w -> w));

        List<Long> seqtsweekList = weekBySeqts.values().stream()
                .map(TprjProjectTsWeek::getSeqtsweek).collect(Collectors.toList());
        Map<Long, TprjProjectTsWeekDetail> detailBySeqtsweek = seqtsweekList.isEmpty() ? Map.of()
                : tsWeekDetailRepository.findBySeqtsweekIn(seqtsweekList)
                        .stream().collect(Collectors.toMap(TprjProjectTsWeekDetail::getSeqtsweek, d -> d));

        Map<Long, String> projectNameCache = new HashMap<>();

        return schedules.stream()
                .map(schedule -> {
                    TprjProjectScheduleTs schedulets = scheduletsBySeqschedule.get(schedule.getSeqschedule());
                    TprjProjectTsWeek week = schedulets == null ? null : weekBySeqts.get(schedulets.getSeqts());
                    TprjProjectTsWeekDetail detail = week == null ? null : detailBySeqtsweek.get(week.getSeqtsweek());
                    return toView(schedule, week, detail, projectNameCache);
                })
                .filter(v -> statusFilter == null || statusFilter.equals(v.getStatus()))
                .sorted(Comparator.comparing(WeeklyProgressActivityView::getProjectName, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WeeklyProgressActivityView::getActivitydesc, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Transactional
    public WeeklyProgressActivityView saveWeek(WeeklyProgressWeekRequest request) {
        if (request.getSeqschedule() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seqschedule es requerido");
        }
        if (request.getWeekStart() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weekStart es requerido");
        }

        TprjProjectSchedule schedule = scheduleRepository.findById(request.getSeqschedule())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));

        if (schedule.getMemberuser() == null || !schedule.getMemberuser().equals(AuthContext.currentUserCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el consultor asignado puede registrar el avance de esta actividad");
        }

        LocalDate monday = normalizeToMonday(request.getWeekStart());
        LocalDate sunday = monday.plusDays(6);

        boolean scheduletsIsNew = false;
        TprjProjectScheduleTs schedulets = scheduleTsRepository
                .findBySeqprojectAndSeqschedule(schedule.getSeqproject(), schedule.getSeqschedule())
                .orElse(null);
        if (schedulets == null) {
            scheduletsIsNew = true;
            schedulets = new TprjProjectScheduleTs();
            schedulets.setCodeinstance(CODEINSTANCE);
            schedulets.setCodecompany(CODECOMPANY);
            schedulets.setSeqproject(schedule.getSeqproject());
            schedulets.setSeqschedule(schedule.getSeqschedule());
            schedulets.setDatefrom(monday);
            schedulets.setUsercreate(AuthContext.currentUserCode());
            schedulets.setDatecreate(LocalDateTime.now());
        }
        schedulets.setStatementdate(sunday);
        schedulets.setDateto(sunday);
        schedulets.setUserlastmodify(AuthContext.currentUserCode());
        schedulets.setDatemodify(LocalDateTime.now());
        TprjProjectScheduleTs savedSchedulets = scheduleTsRepository.save(schedulets);
        scheduleTsHisRepository.save(TprjProjectScheduleTsHis.snapshotOf(
                savedSchedulets, scheduletsIsNew ? "NEW" : "UPDATE", AuthContext.currentUserCode()));

        boolean weekIsNew = false;
        TprjProjectTsWeek week = tsWeekRepository
                .findBySeqtimesheetsAndDatefrom(savedSchedulets.getSeqts(), monday)
                .orElse(null);
        if (week == null) {
            weekIsNew = true;
            week = new TprjProjectTsWeek();
            week.setCodeinstance(CODEINSTANCE);
            week.setCodecompany(CODECOMPANY);
            week.setSeqtimesheets(savedSchedulets.getSeqts());
            week.setDatefrom(monday);
            week.setUsercreate(AuthContext.currentUserCode());
            week.setDatecreate(LocalDateTime.now());
        }
        // Toda corrección (aunque ya estuviera APR/REC) vuelve a REG: requiere nueva revisión.
        week.setDateto(sunday);
        week.setStatuscat(STATUSCAT);
        week.setStatus(DEFAULT_STATUS);
        week.setReviewedby(null);
        week.setUserlastmodify(AuthContext.currentUserCode());
        week.setDatemodify(LocalDateTime.now());
        TprjProjectTsWeek savedWeek = tsWeekRepository.save(week);
        tsWeekHisRepository.save(TprjProjectTsWeekHis.snapshotOf(
                savedWeek, weekIsNew ? "NEW" : "UPDATE", AuthContext.currentUserCode()));

        boolean detailIsNew = false;
        TprjProjectTsWeekDetail detail = tsWeekDetailRepository.findBySeqtsweek(savedWeek.getSeqtsweek()).orElse(null);
        if (detail == null) {
            detailIsNew = true;
            detail = new TprjProjectTsWeekDetail();
            detail.setCodeinstance(CODEINSTANCE);
            detail.setCodecompany(CODECOMPANY);
            detail.setSeqtsweek(savedWeek.getSeqtsweek());
            detail.setUsercreate(AuthContext.currentUserCode());
            detail.setDatecreate(LocalDateTime.now());
        }
        detail.setDatefrom(monday);
        detail.setDateto(sunday);
        detail.setDay1Perc(request.getDay1Perc());
        detail.setDay2Perc(request.getDay2Perc());
        detail.setDay3Perc(request.getDay3Perc());
        detail.setDay4Perc(request.getDay4Perc());
        detail.setDay5Perc(request.getDay5Perc());
        detail.setDay6Perc(request.getDay6Perc());
        detail.setDay7Perc(request.getDay7Perc());
        detail.setUserlastmodify(AuthContext.currentUserCode());
        detail.setDatemodify(LocalDateTime.now());
        TprjProjectTsWeekDetail savedDetail = tsWeekDetailRepository.save(detail);
        tsWeekDetailHisRepository.save(TprjProjectTsWeekDetailHis.snapshotOf(
                savedDetail, detailIsNew ? "NEW" : "UPDATE", AuthContext.currentUserCode()));

        BigDecimal lastPerc = lastNonNullDay(savedDetail);
        if (lastPerc != null) {
            BigDecimal denom = schedule.getBasedaystotal() != null && schedule.getBasedaystotal().compareTo(BigDecimal.ZERO) > 0
                    ? schedule.getBasedaystotal()
                    : (schedule.getPlanneddays() != null ? schedule.getPlanneddays() : BigDecimal.ZERO);
            schedule.setAdvrealperc(lastPerc);
            schedule.setAdvrealdays(lastPerc.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    .multiply(denom).setScale(2, RoundingMode.HALF_UP));
            schedule.setDatestamentday(sunday);
            schedule.setUserlastmodify(AuthContext.currentUserCode());
            schedule.setDatemodify(LocalDateTime.now());
            schedule = scheduleRepository.save(schedule);
            scheduleHisRepository.save(TprjProjectScheduleHis.snapshotOf(schedule, "UPDATE", AuthContext.currentUserCode()));
        }

        return toView(schedule, savedWeek, savedDetail, new HashMap<>());
    }

    @Transactional
    public WeeklyProgressActivityView review(Long seqtsweek, WeeklyProgressReviewRequest request) {
        if (!AuthContext.isAuthorizer()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un autorizador puede aprobar/rechazar");
        }
        if (!"APR".equals(request.getStatus()) && !"REC".equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status debe ser APR o REC");
        }

        TprjProjectTsWeek week = tsWeekRepository.findById(seqtsweek)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro semanal no encontrado"));
        week.setStatus(request.getStatus());
        week.setReviewedby(AuthContext.currentUserCode());
        week.setUserlastmodify(AuthContext.currentUserCode());
        week.setDatemodify(LocalDateTime.now());
        TprjProjectTsWeek savedWeek = tsWeekRepository.save(week);
        tsWeekHisRepository.save(TprjProjectTsWeekHis.snapshotOf(savedWeek, "UPDATE", AuthContext.currentUserCode()));

        TprjProjectScheduleTs schedulets = scheduleTsRepository.findById(savedWeek.getSeqtimesheets())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cabecera de seguimiento no encontrada"));
        TprjProjectSchedule schedule = scheduleRepository.findById(schedulets.getSeqschedule())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actividad no encontrada"));
        TprjProjectTsWeekDetail detail = tsWeekDetailRepository.findBySeqtsweek(savedWeek.getSeqtsweek()).orElse(null);

        return toView(schedule, savedWeek, detail, new HashMap<>());
    }

    private BigDecimal lastNonNullDay(TprjProjectTsWeekDetail d) {
        BigDecimal[] days = {
                d.getDay7Perc(), d.getDay6Perc(), d.getDay5Perc(), d.getDay4Perc(),
                d.getDay3Perc(), d.getDay2Perc(), d.getDay1Perc()
        };
        for (BigDecimal v : days) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private WeeklyProgressActivityView toView(TprjProjectSchedule schedule, TprjProjectTsWeek week,
                                               TprjProjectTsWeekDetail detail, Map<Long, String> projectNameCache) {
        String projectName = projectNameCache.computeIfAbsent(schedule.getSeqproject(),
                seq -> projectRepository.findById(seq).map(TprjProject::getProjectName).orElse(null));

        return new WeeklyProgressActivityView(
                schedule.getSeqproject(),
                projectName,
                schedule.getSeqschedule(),
                schedule.getShortactivitydesc(),
                schedule.getActivitydesc(),
                schedule.getMemberuser(),
                nameResolver.userName(schedule.getMemberuser()),
                schedule.getAdvrealperc(),
                week != null ? week.getSeqtsweek() : null,
                detail != null ? detail.getDay1Perc() : null,
                detail != null ? detail.getDay2Perc() : null,
                detail != null ? detail.getDay3Perc() : null,
                detail != null ? detail.getDay4Perc() : null,
                detail != null ? detail.getDay5Perc() : null,
                detail != null ? detail.getDay6Perc() : null,
                detail != null ? detail.getDay7Perc() : null,
                week != null ? week.getStatus() : null,
                week != null ? nameResolver.catalogItemName(week.getStatuscat(), week.getStatus()) : null,
                week != null ? week.getReviewedby() : null,
                week != null ? nameResolver.userName(week.getReviewedby()) : null
        );
    }
}
