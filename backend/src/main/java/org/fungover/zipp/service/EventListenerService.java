package org.fungover.zipp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventListenerService {

    private final SseService sseService;

    public EventListenerService(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void listen(String message) {
        System.out.println("Received: " + message);
        sseService.sendEvent(message);
    }
}