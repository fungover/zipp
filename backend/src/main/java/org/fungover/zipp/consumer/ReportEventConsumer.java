package org.fungover.zipp.consumer;

import org.fungover.zipp.service.SseService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportEventConsumer {

    private final SseService sseService;

    public ReportEventConsumer(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "reports", groupId = "report-group")
    public void consume(String message) {
        sseService.send("id123", message);
    }
}
