package org.fungover.zipp.repository;

import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

  List<ReportEntity> findAllBySubmittedByUserId(Long userId);

  List<ReportEntity> findAllByStatusAndSubmittedAtBefore(ReportStatus status, Instant date);
}
