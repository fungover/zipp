package org.fungover.zipp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerService.class);

    private final SseService sseService;

    public EventListenerService(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void listen(ReportEvent event) {
        LOGGER.info("Received: {}", event);
        sseService.send(event.id(), event);
    }
}
