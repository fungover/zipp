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
    public void listen(String message) {
        System.out.println("Received: " + message);

        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String id = parts[0];
            String payload = parts[1];

            sseService.send(id, Map.of("type", "report", "payload", payload));
        } else {
            sseService.send("default", Map.of("type", "report", "payload", message));
        }
    }
}