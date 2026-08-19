package com.llacsaa.timesheet.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TeamMemberView {
    private Long seqteam;
    private Long memberuser;
    private String memberName;
    private String projectrol;
    private LocalDate assignmentdate;
}
