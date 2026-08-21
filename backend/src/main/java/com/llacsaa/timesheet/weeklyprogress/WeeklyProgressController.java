package com.llacsaa.timesheet.weeklyprogress;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule-progress")
public class WeeklyProgressController {

    private final WeeklyProgressService weeklyProgressService;

    public WeeklyProgressController(WeeklyProgressService weeklyProgressService) {
        this.weeklyProgressService = weeklyProgressService;
    }

    @GetMapping("/activities")
    public List<WeeklyProgressActivityView> listActivities(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) Long seqproject,
            @RequestParam(required = false) Long memberuser,
            @RequestParam(required = false) String status
    ) {
        return weeklyProgressService.listActivities(weekStart, seqproject, memberuser, status);
    }

    @PutMapping("/week")
    public WeeklyProgressActivityView saveWeek(@RequestBody WeeklyProgressWeekRequest request) {
        return weeklyProgressService.saveWeek(request);
    }

    @PutMapping("/week/{seqtsweek}/review")
    public WeeklyProgressActivityView review(@PathVariable Long seqtsweek, @RequestBody WeeklyProgressReviewRequest request) {
        return weeklyProgressService.review(seqtsweek, request);
    }
}
