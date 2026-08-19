package com.llacsaa.timesheet.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<TprjProjectNews, Long> {
    List<TprjProjectNews> findBySeqprojectOrderByDatenewarrivalDesc(Long seqproject);
}
