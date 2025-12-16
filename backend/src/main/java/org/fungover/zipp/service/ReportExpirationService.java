package org.fungover.zipp.service;

import org.fungover.zipp.config.ReportConfig;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportExpirationService {

    private static final Logger LOG =
        LoggerFactory.getLogger(ReportExpirationService.class);

    private final ReportRepository reportRepository;
    private final ReportConfig reportConfig;

    public ReportExpirationService(
        ReportRepository reportRepository,
        ReportConfig reportConfig
    ) {
        this.reportRepository = reportRepository;
        this.reportConfig = reportConfig;
    }

    /**
     * Marks old ACTIVE reports as EXPIRED and sets expiredAt timestamp.
     */
    @Scheduled(cron = "${cleanup.expire-cron}")
    @Transactional
    public void expireOldReports() {
        if (reportConfig.getExpirationHours() <= 0) {
            return;
        }

        Instant expirationThreshold =
            Instant.now().minus(reportConfig.getExpirationHours(), ChronoUnit.HOURS);

        List<ReportEntity> toExpire =
            reportRepository.findAllByStatusAndSubmittedAtBefore(
                ReportStatus.ACTIVE,
                expirationThreshold
            );

        Instant now = Instant.now();

        for (ReportEntity report : toExpire) {
            report.setStatus(ReportStatus.EXPIRED);
            report.setExpiredAt(now);
        }

        reportRepository.saveAll(toExpire);
        LOG.info("Expired {} reports", toExpire.size());
    }

    /**
     * Permanently deletes reports that have been expired long enough.
     */
    @Scheduled(cron = "${cleanup.delete-cron}")
    @Transactional
    public void deleteExpiredReports() {
        if (reportConfig.getDeleteExpiredAfterDays() <= 0) {
            return;
        }

        Instant deleteThreshold =
            Instant.now().minus(
                reportConfig.getDeleteExpiredAfterDays(),
                ChronoUnit.DAYS
            );

        List<ReportEntity> toDelete =
            reportRepository.findAllByStatusAndExpiredAtBefore(
                ReportStatus.EXPIRED,
                deleteThreshold
            );

        reportRepository.deleteAll(toDelete);
        LOG.info("Deleted {} expired reports", toDelete.size());
    }
}
