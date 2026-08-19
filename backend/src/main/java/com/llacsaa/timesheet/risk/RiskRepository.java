package com.llacsaa.timesheet.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<TprjProjectRisk, Long> {
    List<TprjProjectRisk> findBySeqprojectOrderByRiskdateDesc(Long seqproject);
}
