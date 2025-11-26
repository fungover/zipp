package org.fungover.zipp.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record Report(
        String id,
        @NotBlank String description,
        ReportType eventType,
        @NotBlank double latitude,
        @NotBlank double longitude,
        LocalDate submittedAt,
        @NotBlank ReportStatus status,
        String imageUrl
) {
}