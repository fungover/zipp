package org.fungover.zipp.service;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportImageEntity;
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

    private final GeometryFactory geometryFactory;
    private final ReportRepository reportRepository;

    public ReportService(GeometryFactory geometryFactory, ReportRepository reportRepository) {
      this.geometryFactory = geometryFactory;
      this.reportRepository = reportRepository;
    }

    @Transactional
    public Report createReport(Report dto) {
      Point point = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
      point.setSRID(4326);


      ReportEntity entity = new ReportEntity(
              dto.submittedByUserId(),
              dto.description(),
              dto.eventType(),
              point,
              Instant.now(),
              ReportStatus.ACTIVE,
              new HashSet<>()
      );

      if (dto.imageUrls() != null) {
        for (String url : dto.imageUrls()) {
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
    List<ReportEntity> reports = reportRepository.findAllByStatus(ReportStatus.ACTIVE);

    if (reports.isEmpty()) {
      return List.of();
    } else {
      return reports.stream().map(entity -> new Report(
              entity.getSubmittedByUserId(),
              entity.getDescription(),
              entity.getEventType(),
              entity.getCoordinates() != null ? entity.getCoordinates().getY() : 0,
              entity.getCoordinates() != null ? entity.getCoordinates().getX() : 0,
              entity.getSubmittedAt(),
              entity.getStatus(),
              entity.getImages().stream()
                      .map(ReportImageEntity::getImageUrl)
                      .toList()
      )).toList();
    }
  }

  private Report toDto(ReportEntity savedEntity) {
    List<String> images = savedEntity.getImages().stream()
            .map(ReportImageEntity::getImageUrl)
            .toList();

    return new Report(
            savedEntity.getSubmittedByUserId(),
            savedEntity.getDescription(),
            savedEntity.getEventType(),
            savedEntity.getCoordinates().getY(),
            savedEntity.getCoordinates().getX(),
            savedEntity.getSubmittedAt(),
            savedEntity.getStatus(),
            images
    );
  }
}
