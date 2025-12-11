package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;
    private final KafkaTemplate<String, Report> template;

    public ReportController(ReportService reportService, KafkaTemplate<String, Report> template) {
        this.reportService = reportService;
        this.template = template;
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report reportRequest) {
        LOG.info("Report received: {}", reportRequest);

        var newReport = reportService.createReport(reportRequest);

        /*
         * For now the userId is provided by the client later this can be replaced with
         * SecurityContextHolder.getContext().getAuthentication()
         */

        template.send("report", newReport)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    LOG.error("Failed to publish report to Kafka: {}", newReport, ex);
                } else {
                    LOG.debug("Report published to Kafka: {}", newReport);
                }
            });
        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
