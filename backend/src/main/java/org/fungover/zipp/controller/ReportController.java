package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report reportRequest) {
        LOG.info("Report received: {}", reportRequest);

        var newReport = reportService.createReport(reportRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Report>> getAllReports(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getAllReportsForUser(userId));
    }
}
