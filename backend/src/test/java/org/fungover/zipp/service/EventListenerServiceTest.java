package org.fungover.zipp.service;

import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.kafka.ReportType;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventListenerServiceTest {

    @Test
    void listenerShouldForwardEventToSseService() {
        SseService sseService = mock(SseService.class);
        EventListenerService listener = new EventListenerService(sseService);

        ReportAvro event = ReportAvro.newBuilder()
            .setSubmittedByUserId("map123")
            .setDescription("test")
            .setEventType(ReportType.ACCIDENT)
            .setLatitude(1.0)
            .setLongitude(2.0)
            .build();

        listener.listen(event);

        verify(sseService).send("map123", event);
    }

}
