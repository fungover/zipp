package org.fungover.zipp.service;

import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.kafka.ReportStatus;
import org.fungover.zipp.kafka.ReportType;
import org.fungover.zipp.mapper.ReportAvroToDtoMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventListenerServiceTest {

    @Test
    void listenerShouldForwardEventToSseService() {
        SseService sseService = mock(SseService.class);
        ReportAvroToDtoMapper mapper = new ReportAvroToDtoMapper();
        EventListenerService listener = new EventListenerService(sseService, mapper);

        ReportAvro event = ReportAvro.newBuilder().setSubmittedByUserId("map123").setDescription("test")
                .setEventType(ReportType.ACCIDENT).setLatitude(1.0).setLongitude(2.0).setSubmittedAt(Instant.now())
                .setStatus(ReportStatus.ACTIVE).setImageUrls(null).build();

        listener.listen(event);

        verify(sseService).send("map123", mapper.toDto(event));
    }

}
