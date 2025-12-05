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

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report reportRequest) {
        log.info("Report received: {}", reportRequest);

        var newReport = reportService.createReport(reportRequest);

        /*
         * For now the userId is provided by the client later this can be replaced with
         * SecurityContextHolder.getContext().getAuthentication()
         * 
         * And Spring kafka later var cf = template.send("report", newReport);
         * cf.join();
         */

        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
