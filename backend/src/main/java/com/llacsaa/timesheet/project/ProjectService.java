package com.llacsaa.timesheet.project;

import com.llacsaa.timesheet.common.NameResolver;
import com.llacsaa.timesheet.project.dto.ProgressRequest;
import com.llacsaa.timesheet.project.dto.ProjectDetail;
import com.llacsaa.timesheet.project.dto.ProjectListItem;
import com.llacsaa.timesheet.project.dto.ProjectSaveRequest;
import com.llacsaa.timesheet.project.dto.TeamMemberRequest;
import com.llacsaa.timesheet.project.dto.TeamMemberView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;
import com.llacsaa.timesheet.auth.AuthContext;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHisRepository projectHisRepository;
    private final ProjTeamRepository teamRepository;
    private final ProjTeamHisRepository teamHisRepository;
    private final NameResolver nameResolver;

    public ProjectService(ProjectRepository projectRepository,
                           ProjectHisRepository projectHisRepository,
                           ProjTeamRepository teamRepository,
                           ProjTeamHisRepository teamHisRepository,
                           NameResolver nameResolver) {
        this.projectRepository = projectRepository;
        this.projectHisRepository = projectHisRepository;
        this.teamRepository = teamRepository;
        this.teamHisRepository = teamHisRepository;
        this.nameResolver = nameResolver;
    }

    public List<ProjectListItem> listProjects() {
        return projectRepository.findByCodeinstanceAndCodecompanyOrderBySeqDesc(CODEINSTANCE, CODECOMPANY)
                .stream()
                .map(p -> new ProjectListItem(
                        p.getSeq(),
                        p.getRequestDate(),
                        p.getProjectCode(),
                        p.getProjectName(),
                        nameResolver.customerName(p.getCodecustomer()),
                        p.getCodeuserPm(),
                        nameResolver.userName(p.getCodeuserPm()),
                        daysBetween(p.getBaseStartDate(), p.getBaseEndDate()),
                        p.getBaseStartDate(),
                        p.getBaseEndDate(),
                        daysBetween(p.getPlannedStartDate(), p.getPlannedEndDate()),
                        p.getPlannedStartDate(),
                        p.getPlannedEndDate(),
                        nameResolver.catalogItemName(p.getStatuscat(), p.getStatus())
                ))
                .collect(Collectors.toList());
    }

    public ProjectDetail getProject(Long seq) {
        return toDetail(findOrThrow(seq));
    }

    @Transactional
    public ProjectDetail createProject(ProjectSaveRequest request) {
        TprjProject p = new TprjProject();
        p.setCodeinstance(CODEINSTANCE);
        p.setCodecompany(CODECOMPANY);
        applyRequest(p, request);
        p.setUsercreate(AuthContext.currentUserCode());
        p.setUserlastmodify(AuthContext.currentUserCode());
        p.setDatecreate(LocalDateTime.now());
        p.setDatemodify(LocalDateTime.now());

        TprjProject saved = projectRepository.save(p);
        projectHisRepository.save(TprjProjectHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toDetail(saved);
    }

    @Transactional
    public ProjectDetail updateProject(Long seq, ProjectSaveRequest request) {
        TprjProject p = findOrThrow(seq);
        applyRequest(p, request);
        p.setUserlastmodify(AuthContext.currentUserCode());
        p.setDatemodify(LocalDateTime.now());

        TprjProject saved = projectRepository.save(p);
        projectHisRepository.save(TprjProjectHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toDetail(saved);
    }

    @Transactional
    public void deleteProject(Long seq) {
        TprjProject p = findOrThrow(seq);
        projectHisRepository.save(TprjProjectHis.snapshotOf(p, "DELETE", AuthContext.currentUserCode()));
        projectRepository.delete(p);
    }

    // ---- Avance (Fase 4: "Registrar avance") ----

    @Transactional
    public ProjectDetail registerProgress(Long seq, ProgressRequest request) {
        TprjProject p = findOrThrow(seq);
        p.setLastcutoffdate(request.getLastcutoffdate());
        p.setAdvexpectedperc(request.getAdvexpectedperc());
        p.setAdvrealperc(request.getAdvrealperc());
        p.setAdvexpecteddays(request.getAdvexpecteddays());
        p.setAdvrealdays(request.getAdvrealdays());
        p.setDaysconsumed(request.getDaysconsumed());
        p.setVaradvplannedperc(request.getVaradvplannedperc());
        p.setEfectivityperc(request.getEfectivityperc());
        p.setUserlastmodify(AuthContext.currentUserCode());
        p.setDatemodify(LocalDateTime.now());

        TprjProject saved = projectRepository.save(p);
        projectHisRepository.save(TprjProjectHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toDetail(saved);
    }

    // ---- Equipo de trabajo ----

    public List<TeamMemberView> listTeam(Long seqproject) {
        return teamRepository.findBySeqprojectOrderBySeqteam(seqproject)
                .stream()
                .map(t -> new TeamMemberView(
                        t.getSeqteam(),
                        t.getMemberuser(),
                        nameResolver.userName(t.getMemberuser()),
                        t.getProjectrol(),
                        t.getAssignmentdate()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamMemberView addTeamMember(Long seqproject, TeamMemberRequest request) {
        findOrThrow(seqproject);

        TprjProjTeam t = new TprjProjTeam();
        t.setCodeinstance(CODEINSTANCE);
        t.setCodecompany(CODECOMPANY);
        t.setSeqproject(seqproject);
        t.setMemberuser(request.getMemberuser());
        t.setProjectrol(request.getProjectrol());
        t.setAssignmentdate(request.getAssignmentdate());
        t.setUsercreate(AuthContext.currentUserCode());
        t.setUserlastmodify(AuthContext.currentUserCode());
        t.setDatecreate(LocalDateTime.now());
        t.setDatemodify(LocalDateTime.now());

        TprjProjTeam saved = teamRepository.save(t);
        teamHisRepository.save(TprjProjTeamHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return new TeamMemberView(saved.getSeqteam(), saved.getMemberuser(),
                nameResolver.userName(saved.getMemberuser()), saved.getProjectrol(), saved.getAssignmentdate());
    }

    @Transactional
    public void removeTeamMember(Long seqproject, Long seqteam) {
        TprjProjTeam t = teamRepository.findById(seqteam)
                .filter(m -> m.getSeqproject().equals(seqproject))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integrante no encontrado"));
        teamHisRepository.save(TprjProjTeamHis.snapshotOf(t, "DELETE", AuthContext.currentUserCode()));
        teamRepository.delete(t);
    }

    // ---- helpers ----

    private TprjProject findOrThrow(Long seq) {
        return projectRepository.findById(seq)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado: " + seq));
    }

    private void validateDateOrder(LocalDate start, LocalDate end, String message) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void applyRequest(TprjProject p, ProjectSaveRequest r) {
        validateDateOrder(r.getBaseStartDate(), r.getBaseEndDate(),
                "La fecha fin base no puede ser anterior a la fecha inicio base");
        validateDateOrder(r.getPlannedStartDate(), r.getPlannedEndDate(),
                "La fecha fin planificación no puede ser anterior a la fecha inicio planificación");
        validateDateOrder(r.getRealStartDate(), r.getRealEndDate(),
                "La fecha fin real no puede ser anterior a la fecha inicio real");
        p.setContractNumber(r.getContractNumber());
        p.setProjectCode(r.getProjectCode());
        p.setProjectName(r.getProjectName());
        p.setProjectDescription(r.getProjectDescription());
        p.setCodecustomer(r.getCodecustomer());
        p.setCodeuserPm(r.getCodeuserPm());
        p.setRequestDate(r.getRequestDate());
        p.setProjectDuration(r.getProjectDuration());
        p.setBaseStartDate(r.getBaseStartDate());
        p.setBaseEndDate(r.getBaseEndDate());
        p.setPlannedStartDate(r.getPlannedStartDate());
        p.setPlannedEndDate(r.getPlannedEndDate());
        p.setRealStartDate(r.getRealStartDate());
        p.setRealEndDate(r.getRealEndDate());
        p.setStatuscat(r.getStatuscat());
        p.setStatus(r.getStatus());
    }

    private ProjectDetail toDetail(TprjProject p) {
        return new ProjectDetail(
                p.getSeq(),
                p.getContractNumber(),
                p.getProjectCode(),
                p.getProjectName(),
                p.getProjectDescription(),
                p.getCodecustomer(),
                nameResolver.customerName(p.getCodecustomer()),
                p.getRequestDate(),
                p.getCodeuserPm(),
                nameResolver.userName(p.getCodeuserPm()),
                p.getProjectDuration(),
                p.getBaseStartDate(),
                p.getBaseEndDate(),
                p.getPlannedStartDate(),
                p.getPlannedEndDate(),
                p.getRealStartDate(),
                p.getRealEndDate(),
                p.getStatuscat(),
                p.getStatus(),
                nameResolver.catalogItemName(p.getStatuscat(), p.getStatus()),
                p.getLastcutoffdate(),
                p.getAdvexpectedperc(),
                p.getAdvrealperc(),
                p.getAdvexpecteddays(),
                p.getAdvrealdays(),
                p.getDaysconsumed(),
                p.getVaradvplannedperc(),
                p.getEfectivityperc(),
                p.getUsercreate(),
                nameResolver.userName(p.getUsercreate()),
                p.getDatecreate(),
                p.getUserlastmodify(),
                nameResolver.userName(p.getUserlastmodify()),
                p.getDatemodify()
        );
    }

    private Long daysBetween(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(start, end);
    }
}
