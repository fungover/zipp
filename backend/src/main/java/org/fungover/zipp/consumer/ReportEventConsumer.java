package org.fungover.zipp.consumer;

import org.fungover.zipp.service.SseService;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import java.sql.SQLOutput;

@Service
public class ReportEventConsumer {

    private final SseService sseService;
    private final HandlerMapping resourceHandlerMapping;

    public ReportEventConsumer(SseService sseService, @Nullable HandlerMapping resourceHandlerMapping) {
        this.sseService = sseService;
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    @KafkaListener(topics = "reports", groupId = "zipp")
    public void consume(String message) {
        System.out.println("Received from Kafka: " + message);
        sseService.send("id123", message);
    }
}
