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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.eq;


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

        verify(reportConfig, times(2)).getExpirationHours(); // Called twice in the service
        verify(reportRepository).findAllByStatusAndSubmittedAtBefore(eq(ReportStatus.ACTIVE), instantCaptor.capture());

        Instant threshold = instantCaptor.getValue();
        Instant expectedThreshold = Instant.now().minus(48, ChronoUnit.HOURS);
        assertTrue(Math.abs(threshold.toEpochMilli() - expectedThreshold.toEpochMilli()) < 1000);

        verify(reportRepository).saveAll(reportListCaptor.capture());
        List<ReportEntity> savedReports = reportListCaptor.getValue();
        assertEquals(2, savedReports.size());
        assertTrue(savedReports.stream().allMatch(r -> r.getStatus() == ReportStatus.EXPIRED));
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

    private ReportEntity createReport(Long id, ReportStatus status, Instant submittedAt) {
        ReportEntity report = new ReportEntity();
        report.setId(id);
        report.setStatus(status);
        report.setSubmittedAt(submittedAt);
        return report;
    }
}
