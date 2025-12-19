package org.fungover.zipp.service;

import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.kafka.ReportAvro;
import org.fungover.zipp.mapper.ReportAvroToDtoMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerService.class);
    private final ReportAvroToDtoMapper mapper;
    private final SseService sseService;

    public EventListenerService(SseService sseService, ReportAvroToDtoMapper mapper) {
        this.sseService = sseService;
        this.mapper = mapper;

    }

    @KafkaListener(topics = "${app.kafka.topic.report}", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listen(ReportAvro event) {
        ReportResponse dto = mapper.toDto(event);

        sseService.send(dto.submittedByUserId(), dto);
    }
}
