package org.fungover.zipp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;

import java.time.Instant;
import java.util.List;

public record ReportResponse(@NotNull String submittedByUserId, @NotBlank String description,
                             @NotNull ReportType eventType,

                             @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") @NotNull Double latitude,

                             @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") @NotNull Double longitude,

                             Instant submittedAt, ReportStatus status, List<String> imageUrls) {
}
