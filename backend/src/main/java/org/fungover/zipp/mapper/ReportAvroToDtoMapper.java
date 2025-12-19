package org.fungover.zipp.mapper;

import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.kafka.ReportAvro;
import org.springframework.stereotype.Component;

@Component
public class ReportAvroToDtoMapper {

    public ReportResponse toDto(ReportAvro avro) {
        return new ReportResponse(avro.getSubmittedByUserId().toString(), avro.getDescription().toString(),
                org.fungover.zipp.entity.ReportType.valueOf(avro.getEventType().name()), avro.getLatitude(),
                avro.getLongitude(), avro.getSubmittedAt(),
                avro.getStatus() != null ? org.fungover.zipp.entity.ReportStatus.valueOf(avro.getStatus().name()) : null,
                avro.getImageUrls() == null ? null : avro.getImageUrls().stream().map(CharSequence::toString).toList());
    }
}
