package org.fungover.zipp.service;

import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class EventListenerServiceTest {

    @Test
    void listenerShouldForwardEventToSseService() {

        SseService sseService = spy(SseService.class);
        EventListenerService listener = new EventListenerService(sseService);

        ReportResponse event = new ReportResponse("map123", "Hello World", ReportType.ACCIDENT, 59.0, 18.0,
                Instant.now(), ReportStatus.ACTIVE, List.of());

        listener.listen(event);

        verify(sseService).send("map123", event);
    }
}
