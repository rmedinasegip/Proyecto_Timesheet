package com.llacsaa.timesheet.change;

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
@RequestMapping("/api/projects/{seqproject}/changes")
public class ChangeController {

    private final ChangeService changeService;

    public ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @GetMapping
    public List<ChangeView> list(@PathVariable Long seqproject) {
        return changeService.listByProject(seqproject);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeView create(@PathVariable Long seqproject, @RequestBody ChangeRequest request) {
        return changeService.create(seqproject, request);
    }

    @PutMapping("/{seqchange}")
    public ChangeView update(@PathVariable Long seqproject, @PathVariable Long seqchange, @RequestBody ChangeRequest request) {
        return changeService.update(seqproject, seqchange, request);
    }

    @DeleteMapping("/{seqchange}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seqproject, @PathVariable Long seqchange) {
        changeService.delete(seqproject, seqchange);
    }
}
