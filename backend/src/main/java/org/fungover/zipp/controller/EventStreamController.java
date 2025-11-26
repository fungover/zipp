package org.fungover.zipp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EventStreamController {

    @GetMapping("/events")
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter();
        // TODO: Hook into EventListenerService
        return emitter;
    }
}
