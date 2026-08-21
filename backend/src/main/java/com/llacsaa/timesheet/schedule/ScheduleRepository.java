package com.llacsaa.timesheet.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<TprjProjectSchedule, Long> {
    List<TprjProjectSchedule> findBySeqprojectOrderBySeqschedule(Long seqproject);

    List<TprjProjectSchedule> findBySeqscheduleparent(Long seqscheduleparent);

    /** Actividades "Hijo" asignadas a un consultor, a través de todos los proyectos. */
    List<TprjProjectSchedule> findByMemberuser(Long memberuser);

    /** Todas las actividades "Hijo" (memberuser no nulo), para la vista de autorizador. */
    List<TprjProjectSchedule> findByMemberuserIsNotNull();
}
