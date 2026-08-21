package com.llacsaa.timesheet.weeklyprogress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TsWeekDetailRepository extends JpaRepository<TprjProjectTsWeekDetail, Long> {
    Optional<TprjProjectTsWeekDetail> findBySeqtsweek(Long seqtsweek);

    List<TprjProjectTsWeekDetail> findBySeqtsweekIn(List<Long> seqtsweekList);
}
