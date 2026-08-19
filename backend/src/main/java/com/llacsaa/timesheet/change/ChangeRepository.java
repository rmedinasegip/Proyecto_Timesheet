package com.llacsaa.timesheet.change;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeRepository extends JpaRepository<TprjProjectChange, Long> {
    List<TprjProjectChange> findBySeqprojectOrderByChangedateDesc(Long seqproject);
}
