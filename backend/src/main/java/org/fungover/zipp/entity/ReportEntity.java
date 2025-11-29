package org.fungover.zipp.entity;

import jakarta.persistence.*;
import org.fungover.zipp.dto.ReportStatus;
import org.fungover.zipp.dto.ReportType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reports")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // TODO: Replace this field with the proper @ManyToOne relationship once Issue #4 is ready
    @Column(name = "submitted_by_user_id", nullable = false)
    private Long submittedByUserId;

    /* Target Implementation:
     * @ManyToOne
     * @JoinColumn(name = "user_id", nullable = false)
     * private UserEntity user;
     */

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType eventType;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(columnDefinition = "POINT SRID 4326")
    private Point coordinates;

    @Column(nullable = false)
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReportImageEntity> images = new HashSet<>();

    public ReportEntity() {
    }

    public ReportEntity(Long submittedByUserId,
                        String description,
                        ReportType eventType,
                        Point coordinates,
                        Instant submittedAt,
                        ReportStatus status,
                        Set<ReportImageEntity> images) {
        this.submittedByUserId = submittedByUserId;
        this.description = description;
        this.eventType = eventType;
        this.coordinates = coordinates;
        this.submittedAt = submittedAt;
        this.status = status;
        this.images = images != null ? images : new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportType getEventType() {
        return eventType;
    }

    public void setEventType(ReportType eventType) {
        this.eventType = eventType;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Long getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(Long submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public Set<ReportImageEntity> getImages() {
        return images;
    }

    public void setImages(Set<ReportImageEntity> images) {
        this.images = images;
    }
}
