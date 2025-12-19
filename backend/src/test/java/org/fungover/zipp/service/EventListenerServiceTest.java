package org.fungover.zipp.service;

<<<<<<< HEAD
import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.kafka.ReportStatus;
import org.fungover.zipp.kafka.ReportType;
import org.fungover.zipp.mapper.ReportAvroToDtoMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
=======
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.spy;
>>>>>>> 1c0b1f60b0edbfe7e17e6d076fe2a86ad0586ad2
import static org.mockito.Mockito.verify;

class EventListenerServiceTest {

    @Test
    void listenerShouldForwardEventToSseService() {
<<<<<<< HEAD
        SseService sseService = mock(SseService.class);
        ReportAvroToDtoMapper mapper = new ReportAvroToDtoMapper();
        EventListenerService listener = new EventListenerService(sseService, mapper);

        ReportAvro event = ReportAvro.newBuilder().setSubmittedByUserId("map123").setDescription("test")
                .setEventType(ReportType.ACCIDENT).setLatitude(1.0).setLongitude(2.0).setSubmittedAt(Instant.now())
                .setStatus(ReportStatus.ACTIVE).setImageUrls(null).build();

        listener.listen(event);

        verify(sseService).send("map123", mapper.toDto(event));
    }

=======

        SseService sseService = spy(new SseService());
        EventListenerService listener = new EventListenerService(sseService);

        ReportResponse event = new ReportResponse("map123", "Hello World", ReportType.ACCIDENT, 59.0, 18.0,
                Instant.now(), ReportStatus.ACTIVE, List.of());

        listener.listen(event);

        verify(sseService).send("map123", event);
    }
>>>>>>> 1c0b1f60b0edbfe7e17e6d076fe2a86ad0586ad2
}
