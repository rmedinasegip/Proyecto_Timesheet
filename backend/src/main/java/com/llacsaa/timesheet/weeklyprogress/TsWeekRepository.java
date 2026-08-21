package com.llacsaa.timesheet.weeklyprogress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TsWeekRepository extends JpaRepository<TprjProjectTsWeek, Long> {
    Optional<TprjProjectTsWeek> findBySeqtimesheetsAndDatefrom(Long seqtimesheets, LocalDate datefrom);

    List<TprjProjectTsWeek> findBySeqtimesheetsInAndDatefrom(List<Long> seqtimesheetsList, LocalDate datefrom);
}
