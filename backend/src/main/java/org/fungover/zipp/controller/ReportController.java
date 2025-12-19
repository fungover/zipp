package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.service.ReportEventPublisher;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final ReportEventPublisher reportEventPublisher;
    private final UserIdentityService userIdentityService;

    public ReportController(ReportService reportService, ReportEventPublisher reportEventPublisher,
            UserIdentityService userIdentityService) {
        this.reportService = reportService;
        this.reportEventPublisher = reportEventPublisher;
        this.userIdentityService = userIdentityService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody Report reportRequest,
            Authentication authentication) {

        LOG.info("Report received: {}", reportRequest);
        User currentUser = userIdentityService.getCurrentUser(authentication);
        ReportResponse newReport = reportService.createReport(currentUser, reportRequest);
        reportEventPublisher.publishReportCreated(newReport);

        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
