package org.fungover.zipp.repository;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    List<ReportEntity> findAllByStatus(ReportStatus status);

    List<ReportEntity> findAllBySubmittedByEmail(String email);

    List<ReportEntity> findAllByStatusAndSubmittedAtBefore(ReportStatus reportStatus, Instant expirationThreshold);

    List<ReportEntity> findAllByEventTypeAndStatusAndSubmittedAtAfter(ReportType eventType, ReportStatus status, Instant submittedAfter);
}
