package com.llacsaa.timesheet.project;

import com.llacsaa.timesheet.project.dto.ProgressRequest;
import com.llacsaa.timesheet.project.dto.ProjectDetail;
import com.llacsaa.timesheet.project.dto.ProjectListItem;
import com.llacsaa.timesheet.project.dto.ProjectSaveRequest;
import com.llacsaa.timesheet.project.dto.TeamMemberRequest;
import com.llacsaa.timesheet.project.dto.TeamMemberView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectListItem> list() {
        return projectService.listProjects();
    }

    @GetMapping("/{seq}")
    public ProjectDetail get(@PathVariable Long seq) {
        return projectService.getProject(seq);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDetail create(@RequestBody ProjectSaveRequest request) {
        return projectService.createProject(request);
    }

    @PutMapping("/{seq}")
    public ProjectDetail update(@PathVariable Long seq, @RequestBody ProjectSaveRequest request) {
        return projectService.updateProject(seq, request);
    }

    @DeleteMapping("/{seq}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seq) {
        projectService.deleteProject(seq);
    }

    @PutMapping("/{seq}/progress")
    public ProjectDetail registerProgress(@PathVariable Long seq, @RequestBody ProgressRequest request) {
        return projectService.registerProgress(seq, request);
    }

    @GetMapping("/{seq}/team")
    public List<TeamMemberView> listTeam(@PathVariable Long seq) {
        return projectService.listTeam(seq);
    }

    @PostMapping("/{seq}/team")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberView addTeamMember(@PathVariable Long seq, @RequestBody TeamMemberRequest request) {
        return projectService.addTeamMember(seq, request);
    }

    @DeleteMapping("/{seq}/team/{seqteam}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeamMember(@PathVariable Long seq, @PathVariable Long seqteam) {
        projectService.removeTeamMember(seq, seqteam);
    }
}
