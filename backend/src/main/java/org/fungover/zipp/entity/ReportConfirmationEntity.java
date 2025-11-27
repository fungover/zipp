package org.fungover.zipp.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class ReportConfirmationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "report_id", nullable = false)
  private ReportEntity report;

  private Long userId;

  private Instant confirmedAt = Instant.now();

  public ReportConfirmationEntity() {
  }

  ReportConfirmationEntity(ReportEntity report, Long userId) {
    this.report = report;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ReportEntity getReport() {
    return report;
  }

  public void setReport(ReportEntity report) {
    this.report = report;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(Instant confirmedAt) {
    this.confirmedAt = confirmedAt;
  }
}
