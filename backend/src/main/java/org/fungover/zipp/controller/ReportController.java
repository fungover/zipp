package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.mapper.ReportAvroMapper;
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
    private final KafkaTemplate<String, ReportAvro> template;
    private final ReportAvroMapper reportAvroMapper;

    public ReportController(ReportService reportService, KafkaTemplate<String, ReportAvro> template, ReportAvroMapper reportAvroMapper) {
        this.reportService = reportService;
        this.template = template;
        this.reportAvroMapper = reportAvroMapper;
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report reportRequest) {
        LOG.info("Report received: {}", reportRequest);

        var newReport = reportService.createReport(reportRequest);

        /*
         * For now the userId is provided by the client later this can be replaced with
         * SecurityContextHolder.getContext().getAuthentication()
         */
        
        var avroReport = reportAvroMapper.toAvro(newReport);
        var cf = template.send("report-avro", String.valueOf(newReport.submittedByUserId()), avroReport);
        cf.join();


        return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }
}
