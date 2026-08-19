package com.llacsaa.timesheet.timesheet;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping
    public List<TimesheetView> search(
            @RequestParam(required = false) Long memberuser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long seqproject
    ) {
        return timesheetService.search(memberuser, dateFrom, dateTo, status, seqproject);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetView create(@RequestBody TimesheetRequest request) {
        return timesheetService.create(request);
    }

    @PutMapping("/{seqts}")
    public TimesheetView update(@PathVariable Long seqts, @RequestBody TimesheetRequest request) {
        return timesheetService.update(seqts, request);
    }

    @PutMapping("/{seqts}/review")
    public TimesheetView review(@PathVariable Long seqts, @RequestBody TimesheetReviewRequest request) {
        return timesheetService.review(seqts, request);
    }

    @DeleteMapping("/{seqts}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seqts) {
        timesheetService.delete(seqts);
    }
}
