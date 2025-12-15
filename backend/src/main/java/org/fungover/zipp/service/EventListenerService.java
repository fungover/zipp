package org.fungover.zipp.service;

import org.fungover.zipp.kafka.ReportAvro;
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

    @KafkaListener(
        topics = "${app.kafka.topic.report}",
        groupId = "#{T(java.util.UUID).randomUUID().toString()}"
    )
    public void listen(ReportAvro event) {
        sseService.send(event.getSubmittedByUserId().toString(), event);
    }
}
