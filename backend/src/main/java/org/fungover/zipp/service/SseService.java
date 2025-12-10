package org.fungover.zipp.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String id) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(id, emitter));
        emitter.onTimeout(() -> remove(id, emitter));
        emitter.onError(ex -> remove(id, emitter));

        return emitter;
    }

    public void send(String id, Object event) {
        var list = emitters.get(id);
        if (list == null)
            return;

        for (var emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(event));
            } catch (Exception e) {
                remove(id, emitter);
            }
        }
    }

    private void remove(String id, SseEmitter emitter) {
        var list = emitters.get(id);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(id);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    public void sendKeepAlive() {
        for (var entry : emitters.entrySet()) {
            String id = entry.getKey();
            for (var emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                } catch (Exception e) {
                    remove(id, emitter);
                }
            }
        }
    }
}