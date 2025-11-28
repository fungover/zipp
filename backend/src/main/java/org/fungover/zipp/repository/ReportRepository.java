package org.fungover.zipp.repository;

import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

  List<ReportEntity> findAllByStatus(ReportStatus status);
}
