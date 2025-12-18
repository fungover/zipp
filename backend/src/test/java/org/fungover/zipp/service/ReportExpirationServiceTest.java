package org.fungover.zipp.service;

import org.fungover.zipp.config.ReportConfig;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportExpirationServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportConfig reportConfig;

    @InjectMocks
    private ReportExpirationService reportExpirationService;

    @Captor
    private ArgumentCaptor<List<ReportEntity>> reportListCaptor;

    @Captor
    private ArgumentCaptor<Instant> instantCaptor;

    private ReportEntity activeReport;
    private ReportEntity expiredReport1;
    private ReportEntity expiredReport2;

    @BeforeEach
    void setUp() {
        activeReport = createReport(1L, ReportStatus.ACTIVE, Instant.now().minus(23, ChronoUnit.HOURS));
        expiredReport1 = createReport(2L, ReportStatus.ACTIVE, Instant.now().minus(49, ChronoUnit.HOURS));
        expiredReport2 = createReport(3L, ReportStatus.ACTIVE, Instant.now().minus(73, ChronoUnit.HOURS));
    }

    @Test
    void shouldExpireOldReports() {
        when(reportConfig.getExpirationHours()).thenReturn(48L);
        List<ReportEntity> oldReports = Arrays.asList(expiredReport1, expiredReport2);
        when(reportRepository.findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class)))
                .thenReturn(oldReports);

        reportExpirationService.expireOldReports();

        // Called twice in the service (check + threshold)
        verify(reportConfig, times(2)).getExpirationHours();
        verify(reportRepository).findAllByStatusAndSubmittedAtBefore(eq(ReportStatus.ACTIVE), instantCaptor.capture());

        Instant threshold = instantCaptor.getValue();
        Instant expectedThreshold = Instant.now().minus(48, ChronoUnit.HOURS);
        assertTrue(Math.abs(threshold.toEpochMilli() - expectedThreshold.toEpochMilli()) < 1000);

        verify(reportRepository).saveAll(reportListCaptor.capture());
        List<ReportEntity> savedReports = reportListCaptor.getValue();
        assertEquals(2, savedReports.size());
        assertTrue(savedReports.stream().allMatch(r -> r.getStatus() == ReportStatus.EXPIRED));
        assertTrue(savedReports.stream().allMatch(r -> r.getExpiredAt() != null));
    }

    @Test
    void shouldHandleNoExpiredReports() {
        when(reportConfig.getExpirationHours()).thenReturn(48L);
        when(reportRepository.findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        reportExpirationService.expireOldReports();

        verify(reportRepository).findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class));
        verify(reportRepository).saveAll(Collections.emptyList());
    }

    @Test
    void shouldHandleSingleExpiredReport() {
        when(reportConfig.getExpirationHours()).thenReturn(48L);
        List<ReportEntity> singleReport = Collections.singletonList(expiredReport1);
        when(reportRepository.findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class)))
                .thenReturn(singleReport);

        reportExpirationService.expireOldReports();

        verify(reportRepository).saveAll(reportListCaptor.capture());
        List<ReportEntity> savedReports = reportListCaptor.getValue();
        assertEquals(1, savedReports.size());
        assertEquals(ReportStatus.EXPIRED, savedReports.get(0).getStatus());
    }

    @Test
    void shouldUseCorrectExpirationThreshold() {
        long customHours = 72L;
        when(reportConfig.getExpirationHours()).thenReturn(customHours);
        when(reportRepository.findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        reportExpirationService.expireOldReports();

        verify(reportRepository).findAllByStatusAndSubmittedAtBefore(eq(ReportStatus.ACTIVE), instantCaptor.capture());

        Instant threshold = instantCaptor.getValue();
        Instant expectedThreshold = Instant.now().minus(customHours, ChronoUnit.HOURS);
        assertTrue(Math.abs(threshold.toEpochMilli() - expectedThreshold.toEpochMilli()) < 1000);
    }

    @Test
    void shouldSetStatusToExpiredForAllReports() {
        when(reportConfig.getExpirationHours()).thenReturn(48L);
        ReportEntity report1 = createReport(1L, ReportStatus.ACTIVE, Instant.now().minus(50, ChronoUnit.HOURS));
        ReportEntity report2 = createReport(2L, ReportStatus.ACTIVE, Instant.now().minus(100, ChronoUnit.HOURS));
        List<ReportEntity> reports = Arrays.asList(report1, report2);
        when(reportRepository.findAllByStatusAndSubmittedAtBefore(any(ReportStatus.class), any(Instant.class)))
                .thenReturn(reports);

        reportExpirationService.expireOldReports();

        verify(reportRepository).saveAll(reportListCaptor.capture());
        List<ReportEntity> savedReports = reportListCaptor.getValue();
        savedReports.forEach(report -> assertEquals(ReportStatus.EXPIRED, report.getStatus(),
                "Report " + report.getId() + " should be EXPIRED"));
    }

    @Test
    void shouldDeleteExpiredReportsOlderThanConfiguredDays() {
        long deleteAfterDays = 30L;
        when(reportConfig.getDeleteExpiredAfterDays()).thenReturn(deleteAfterDays);

        Instant now = Instant.now();

        ReportEntity oldExpired1 = createReport(10L, ReportStatus.EXPIRED,
                now.minus(deleteAfterDays + 5, ChronoUnit.DAYS));
        oldExpired1.setExpiredAt(now.minus(deleteAfterDays + 5, ChronoUnit.DAYS));

        ReportEntity oldExpired2 = createReport(11L, ReportStatus.EXPIRED,
                now.minus(deleteAfterDays + 10, ChronoUnit.DAYS));
        oldExpired2.setExpiredAt(now.minus(deleteAfterDays + 10, ChronoUnit.DAYS));

        List<ReportEntity> toDelete = Arrays.asList(oldExpired1, oldExpired2);

        when(reportRepository.findAllByStatusAndExpiredAtBefore(eq(ReportStatus.EXPIRED), any(Instant.class)))
                .thenReturn(toDelete);

        reportExpirationService.deleteExpiredReports();

        verify(reportRepository).findAllByStatusAndExpiredAtBefore(eq(ReportStatus.EXPIRED), instantCaptor.capture());
        Instant threshold = instantCaptor.getValue();
        Instant expectedThreshold = Instant.now().minus(deleteAfterDays, ChronoUnit.DAYS);
        assertTrue(Math.abs(threshold.toEpochMilli() - expectedThreshold.toEpochMilli()) < 1000);

        verify(reportRepository).deleteAll(toDelete);
    }

    @Test
    void shouldHandleNoExpiredReportsToDelete() {
        when(reportConfig.getDeleteExpiredAfterDays()).thenReturn(30L);
        when(reportRepository.findAllByStatusAndExpiredAtBefore(eq(ReportStatus.EXPIRED), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        reportExpirationService.deleteExpiredReports();

        verify(reportRepository).findAllByStatusAndExpiredAtBefore(eq(ReportStatus.EXPIRED), any(Instant.class));
        verify(reportRepository).deleteAll(Collections.emptyList());
    }

    private ReportEntity createReport(Long id, ReportStatus status, Instant submittedAt) {
        ReportEntity report = new ReportEntity();
        report.setId(id);
        report.setStatus(status);
        report.setSubmittedAt(submittedAt);
        return report;
    }
}
