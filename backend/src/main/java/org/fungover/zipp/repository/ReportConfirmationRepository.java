package org.fungover.zipp.repository;

import org.fungover.zipp.entity.ReportConfirmationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportConfirmationRepository extends JpaRepository<ReportConfirmationEntity, Long> {
  Optional<ReportConfirmationEntity> findByReportIdAndUserId(Long reportId, Long userId);
  int countByReportId(Long reportId);
}