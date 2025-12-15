package org.fungover.zipp.service;

import org.fungover.zipp.config.DuplicateProperties;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DuplicateDetectionServiceTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void findDuplicateReturnsDuplicate() {
        ReportRepository reportRepository = mock(ReportRepository.class);
        DuplicateProperties duplicateProperties = new DuplicateProperties(50, 10);
        DuplicateDetectionService duplicateDetectionService = new DuplicateDetectionService(duplicateProperties,
                reportRepository);

        ReportType reportType = ReportType.ACCIDENT;
        User user = mock(User.class);
        Instant submittedAt = Instant.now();

        Report incoming = new Report(user, "test", reportType, 59.0, 18.0, submittedAt, ReportStatus.ACTIVE, null);

        Point candidatePoint = geometryFactory.createPoint(new Coordinate(18.0, 59.0));
        candidatePoint.setSRID(4326);

        ReportEntity candidate = mock(ReportEntity.class);
        when(candidate.getCoordinates()).thenReturn(candidatePoint);

        when(reportRepository.findAllByEventTypeAndStatusAndSubmittedAtAfter(eq(reportType), eq(ReportStatus.ACTIVE),
                any(Instant.class))).thenReturn(List.of(candidate));

        Optional<ReportEntity> result = duplicateDetectionService.findDuplicate(incoming);

        assertTrue(result.isPresent());
        assertSame(candidate, result.get());

        verify(reportRepository).findAllByEventTypeAndStatusAndSubmittedAtAfter(eq(reportType), eq(ReportStatus.ACTIVE),
                any(Instant.class));
        verifyNoMoreInteractions(reportRepository);
    }

    @Test
    void findDuplicateReturnsEmptyWhenNoDuplicateExists() {
        ReportRepository reportRepository = mock(ReportRepository.class);
        DuplicateProperties duplicateProperties = new DuplicateProperties(50, 10);
        DuplicateDetectionService duplicateDetectionService = new DuplicateDetectionService(duplicateProperties,
                reportRepository);

        ReportType reportType = ReportType.ACCIDENT;
        User user = mock(User.class);
        Instant submittedAt = Instant.now();

        Report incoming = new Report(user, "test", reportType, 59.0, 18.0, submittedAt, ReportStatus.ACTIVE, null);

        Point candidatePoint = geometryFactory.createPoint(new Coordinate(11.97, 57.70));
        candidatePoint.setSRID(4326);

        ReportEntity candidate = mock(ReportEntity.class);
        when(candidate.getCoordinates()).thenReturn(candidatePoint);

        when(reportRepository.findAllByEventTypeAndStatusAndSubmittedAtAfter(eq(reportType), eq(ReportStatus.ACTIVE),
                any(Instant.class))).thenReturn(List.of(candidate));

        Optional<ReportEntity> result = duplicateDetectionService.findDuplicate(incoming);

        assertTrue(result.isEmpty());

        verify(reportRepository).findAllByEventTypeAndStatusAndSubmittedAtAfter(eq(reportType), eq(ReportStatus.ACTIVE),
                any(Instant.class));
        verifyNoMoreInteractions(reportRepository);
    }
}
