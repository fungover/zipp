package org.fungover.zipp.controller;

import org.fungover.zipp.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventStreamController {

    private final SseService sseService;

    public EventStreamController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/events/{id}")
    public SseEmitter streamEvents(@PathVariable String id) {
        return sseService.subscribe(id);
    }
}
