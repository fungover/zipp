package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
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
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;
    private final KafkaTemplate<String, ReportResponse> template;
    private final UserIdentityService userIdentityService;

    public ReportController(ReportService reportService, KafkaTemplate<String, ReportResponse> template,
            UserIdentityService userIdentityService) {
        this.reportService = reportService;
        this.template = template;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody Report reportRequest,
            Authentication authentication) {
        LOG.info("Report received: {}", reportRequest);

        String userId = userIdentityService.getUserId(authentication);

        var newReport = reportService.createReport(userId, reportRequest);

        /*
         * For now the userId is provided by the client later this can be replaced with
         * SecurityContextHolder.getContext().getAuthentication()
         */
        sendReport("report", newReport);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    public void sendReport(String report, ReportResponse newReport) {
        template.send(report, newReport).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish report to Kafka: {}", newReport, ex);
            } else {
                LOG.debug("Report published to Kafka: {}", newReport);
            }
        });
    }
}
