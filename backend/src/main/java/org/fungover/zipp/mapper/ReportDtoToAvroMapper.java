package org.fungover.zipp.mapper;

import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.kafka.ReportStatus;
import org.fungover.zipp.kafka.ReportType;
import org.springframework.stereotype.Component;

@Component
public final class ReportDtoToAvroMapper {

    public ReportAvro toAvro(ReportResponse r) {

        var b = ReportAvro.newBuilder().setSubmittedByUserId(r.submittedByUserId()).setDescription(r.description())
                .setEventType(ReportType.valueOf(r.eventType().name())).setLatitude(r.latitude())
                .setLongitude(r.longitude());

        var imageUrls = r.imageUrls() == null ? null : r.imageUrls().stream().map(s -> (CharSequence) s).toList();

        b.setImageUrls(imageUrls);

        if (r.submittedAt() != null) {
            b.setSubmittedAt(r.submittedAt());
        }

        b.setStatus(r.status() != null ? ReportStatus.valueOf(r.status().name()) : ReportStatus.ACTIVE);

        return b.build();
    }
}
