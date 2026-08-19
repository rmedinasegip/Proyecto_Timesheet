package com.llacsaa.timesheet.risk;

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
@RequestMapping("/api/projects/{seqproject}/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public List<RiskView> list(@PathVariable Long seqproject) {
        return riskService.listByProject(seqproject);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RiskView create(@PathVariable Long seqproject, @RequestBody RiskRequest request) {
        return riskService.create(seqproject, request);
    }

    @PutMapping("/{seqrisk}")
    public RiskView update(@PathVariable Long seqproject, @PathVariable Long seqrisk, @RequestBody RiskRequest request) {
        return riskService.update(seqproject, seqrisk, request);
    }

    @DeleteMapping("/{seqrisk}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seqproject, @PathVariable Long seqrisk) {
        riskService.delete(seqproject, seqrisk);
    }
}
