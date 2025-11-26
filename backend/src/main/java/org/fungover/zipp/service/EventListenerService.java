package org.fungover.zipp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventListenerService {

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void listen(String message) {
        System.out.println("Received: " + message);
        // TODO: Forward to SSE clients
    }
}