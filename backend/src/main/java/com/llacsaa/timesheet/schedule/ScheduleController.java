package com.llacsaa.timesheet.schedule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{seqproject}/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<ScheduleView> list(@PathVariable Long seqproject) {
        return scheduleService.listByProject(seqproject);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleView create(@PathVariable Long seqproject, @RequestBody ScheduleCreateRequest request) {
        return scheduleService.create(seqproject, request);
    }

    @PutMapping("/{seqschedule}")
    public ScheduleView update(@PathVariable Long seqproject, @PathVariable Long seqschedule,
                                @RequestBody ScheduleUpdateRequest request) {
        return scheduleService.update(seqproject, seqschedule, request);
    }

    @DeleteMapping("/{seqschedule}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seqproject, @PathVariable Long seqschedule) {
        scheduleService.delete(seqproject, seqschedule);
    }
}
