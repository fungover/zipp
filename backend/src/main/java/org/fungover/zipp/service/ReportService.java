package org.fungover.zipp.service;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportImageEntity;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.ReportRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
public class ReportService {

    private static final int MAX_URL_LENGTH = 2048;

    private final GeometryFactory geometryFactory;
    private final ReportRepository reportRepository;

    public ReportService(GeometryFactory geometryFactory, ReportRepository reportRepository) {
        this.geometryFactory = geometryFactory;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public Report createReport(User currentUser, Report dto) {
        Point point = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
        point.setSRID(4326);

        ReportEntity entity = new ReportEntity(currentUser, dto.description(), dto.eventType(), point, Instant.now(),
                ReportStatus.ACTIVE, new HashSet<>());

        if (dto.imageUrls() != null) {
            for (String url : dto.imageUrls()) {
                validateImageUrl(url);

                ReportImageEntity image = new ReportImageEntity();
                image.setImageUrl(url);
                image.setReport(entity);
                entity.getImages().add(image);
            }
        }

        return toDto(reportRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAllByStatus(ReportStatus.ACTIVE).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReportsForUser(String userEmail) {
        return reportRepository.findAllBySubmittedByEmail(userEmail).stream().map(this::toDto).toList();
    }

    private void validateImageUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be null or blank");
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("Image URL exceeds maximum length");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Image URL must use HTTP or HTTPS protocol");
        }
    }

    private Report toDto(ReportEntity entity) {
        return new Report(entity.getSubmittedBy(), entity.getDescription(), entity.getEventType(),
                entity.getCoordinates().getY(), // latitude
                entity.getCoordinates().getX(), // longitude
                entity.getSubmittedAt(), entity.getStatus(),
                entity.getImages().stream().map(ReportImageEntity::getImageUrl).toList());
    }
}
