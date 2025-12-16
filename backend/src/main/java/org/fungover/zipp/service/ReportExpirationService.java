package org.fungover.zipp.service;

import org.fungover.zipp.config.ReportConfig;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.repository.ReportRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportExpirationService {

    private final ReportRepository reportRepository;
    private final ReportConfig reportConfig;

    public ReportExpirationService(ReportRepository reportRepository, ReportConfig reportConfig) {
        this.reportRepository = reportRepository;
        this.reportConfig = reportConfig;
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void expireOldReports() {
        if (reportConfig.getExpirationHours() <= 0) {
            return;
        }
        Instant expirationThreshold = Instant.now().minus(reportConfig.getExpirationHours(), ChronoUnit.HOURS);

        List<ReportEntity> toExpire = reportRepository.findAllByStatusAndSubmittedAtBefore(ReportStatus.ACTIVE,
                expirationThreshold);

        for (ReportEntity report : toExpire) {
            report.setStatus(ReportStatus.EXPIRED);
        }

        reportRepository.saveAll(toExpire);
    }
}
