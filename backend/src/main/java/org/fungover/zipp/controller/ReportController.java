package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.service.ReportEventPublisher;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        String userId = userIdentityService.getUserId(authentication);

        var newReport = reportService.createReport(userId, reportRequest);

        reportEventPublisher.publishReportCreated(newReport);

        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
