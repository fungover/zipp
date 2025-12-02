package org.fungover.zipp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class EventListenerService {

    private final SseService sseService;

    public EventListenerService(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void listen(ReportEvent event) {
        System.out.println("Received: " + event);

        sseService.send(event.id(), event);
    }
}