package com.llacsaa.timesheet.project.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TeamMemberRequest {
    private Long memberuser;
    private String projectrol;
    private LocalDate assignmentdate;
}
