package com.llacsaa.timesheet.weeklyprogress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleTsRepository extends JpaRepository<TprjProjectScheduleTs, Long> {
    Optional<TprjProjectScheduleTs> findBySeqprojectAndSeqschedule(Long seqproject, Long seqschedule);

    List<TprjProjectScheduleTs> findBySeqscheduleIn(List<Long> seqscheduleList);
}
