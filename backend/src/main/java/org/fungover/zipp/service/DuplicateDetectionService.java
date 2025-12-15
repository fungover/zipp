package org.fungover.zipp.service;

import org.fungover.zipp.config.DuplicateProperties;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.repository.ReportRepository;
import org.fungover.zipp.util.GeoUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionService {

    private final DuplicateProperties duplicateProperties;
    private final ReportRepository reportRepository;

    @Autowired
    public DuplicateDetectionService(DuplicateProperties duplicateProperties, ReportRepository reportRepository) {
        this.duplicateProperties = duplicateProperties;
        this.reportRepository = reportRepository;
    }

    public Optional<ReportEntity> findDuplicate(Report report) {
        Instant submittedAt = report.submittedAt() != null ? report.submittedAt() : Instant.now();
        Instant cutOff = submittedAt.minus(Duration.ofMinutes(duplicateProperties.getTimeWindowInMinutes()));

        List<ReportEntity> candidates = reportRepository.findAllByEventTypeAndStatusAndSubmittedAtAfter(report.eventType(), ReportStatus.ACTIVE, cutOff);

        return candidates.stream().filter(r -> {
            Point point = r.getCoordinates();
            return GeoUtils.distanceInMeters(report.latitude(), report.longitude(), point.getY(), point.getX()) <= duplicateProperties.getRadiusInMeters();
        }).findFirst();
    }
}
