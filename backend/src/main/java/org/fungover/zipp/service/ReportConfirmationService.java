package org.fungover.zipp.service;

import org.fungover.zipp.entity.ReportConfirmationEntity;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.repository.ReportConfirmationRepository;
import org.fungover.zipp.repository.ReportRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportConfirmationService {

  private final ReportRepository reportRepository;
  private final ReportConfirmationRepository reportConfirmationRepository;

  ReportConfirmationService(ReportRepository reportRepository, ReportConfirmationRepository reportConfirmationRepository) {
    this.reportRepository = reportRepository;
    this.reportConfirmationRepository = reportConfirmationRepository;
  }

  public int confirmReport(Long reportId, Long userId) {
    ReportEntity report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    boolean alreadyConfirmed = reportConfirmationRepository
            .findByReportIdAndUserId(reportId, userId)
            .isPresent();

    if (alreadyConfirmed) {
      throw new IllegalStateException("Report already confirmed");
    }

    reportConfirmationRepository.save(new ReportConfirmationEntity(report, userId));

    return reportConfirmationRepository.countByReportId(reportId);
  }
}
