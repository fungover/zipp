package org.fungover.zipp.consumer;

import org.fungover.zipp.service.ReportEvent;
import org.fungover.zipp.service.SseService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportEventConsumer.class);

    private final SseService sseService;

    public ReportEventConsumer(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void consume(ReportEvent event) {
        LOGGER.info("Received event from Kafka: {}", event);
        sseService.send(event.id(), event.payload());
    }
}
