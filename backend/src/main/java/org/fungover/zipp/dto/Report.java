package org.fungover.zipp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fungover.zipp.entity.ReportType;

import java.util.List;

public record Report(@NotBlank String description, @NotNull ReportType eventType,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") @NotNull Double latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") @NotNull Double longitude,

       List<@NotBlank String> imageUrls) {
}
