package com.llacsaa.timesheet.report;

import com.llacsaa.timesheet.report.dto.ProjectProgressReport;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;

@RestController
public class ProjectProgressReportController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ProjectProgressReportService reportService;

    public ProjectProgressReportController(ProjectProgressReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/api/projects/{seqproject}/progress-report")
    public ProjectProgressReport getReport(
            @PathVariable Long seqproject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {
        return reportService.getReport(seqproject, cutoffDate);
    }

    @GetMapping("/api/projects/{seqproject}/progress-report/excel")
    public ResponseEntity<byte[]> getReportExcel(
            @PathVariable Long seqproject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cutoffDate) {
        ProjectProgressReport report = reportService.getReport(seqproject, cutoffDate);
        byte[] bytes;
        try {
            bytes = ProjectProgressExcelExport.build(report);
        } catch (IOException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el archivo Excel");
        }
        String filename = "avance-proyecto-" + seqproject + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
