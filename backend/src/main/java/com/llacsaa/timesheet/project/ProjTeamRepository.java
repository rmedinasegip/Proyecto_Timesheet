package com.llacsaa.timesheet.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjTeamRepository extends JpaRepository<TprjProjTeam, Long> {
    List<TprjProjTeam> findBySeqprojectOrderBySeqteam(Long seqproject);
}
