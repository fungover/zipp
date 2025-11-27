package org.fungover.zipp.controller;

import org.fungover.zipp.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventStreamController {

    private final SseService sseService;

    public EventStreamController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/events")
    public SseEmitter streamEvents() {
        return sseService.createEmitter();
    }
}
