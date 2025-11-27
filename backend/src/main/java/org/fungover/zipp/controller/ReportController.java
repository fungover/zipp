package org.fungover.zipp.controller;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.service.ReportConfirmationService;
import org.fungover.zipp.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

  private static final Logger log = LoggerFactory.getLogger(ReportController.class);
  private final ReportService reportService;
  private final ReportConfirmationService reportConfirmationService;

  public ReportController(ReportService reportService, ReportConfirmationService reportConfirmationService) {
    this.reportService = reportService;
    this.reportConfirmationService = reportConfirmationService;
  }

  @PostMapping
  public ResponseEntity<Report> createReport(@RequestBody Report reportRequest) {
    log.info("Report received: {}", reportRequest);

    var newReport = reportService.createReport(reportRequest);

    /* For now the userId is provided by the client
     later this can be replaced with SecurityContextHolder.getContext().getAuthentication()

     And Spring kafka later
     var cf = template.send("report", newReport);
      cf.join();
     */

    return ResponseEntity.status(201).body(newReport);
  }

  @GetMapping
  public ResponseEntity<List<Report>> getAllReports(@RequestParam Long userId) {
    return ResponseEntity.ok(reportService.getAllReports(userId));
  }

  @PostMapping("/{id}/confirm")
  public ResponseEntity<Integer> confirmReport(@PathVariable Long id,
                                               @RequestParam Long userId) {
    int totalConfirmations = reportConfirmationService.confirmReport(id, userId);
    return ResponseEntity.ok(totalConfirmations);
  }
}
