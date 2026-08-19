package com.llacsaa.timesheet.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<TprjProject, Long> {
    List<TprjProject> findByCodeinstanceAndCodecompanyOrderBySeqDesc(String codeinstance, String codecompany);
}
