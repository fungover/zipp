package org.fungover.zipp.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

 class EventListenerServiceTest {

     @Test
     void listenerShouldForwardEventToSseService() {

         SseService sseService = mock(SseService.class);
         EventListenerService listener = new EventListenerService(sseService);

         ReportEvent event = new ReportEvent("map123", "Hello World", "report", System.currentTimeMillis());

         listener.listen(event);

         verify(sseService).send("map123", event);
     }
}
