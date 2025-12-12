package org.fungover.zipp.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class ReplayService {

    private final Deque<String> buffer = new ArrayDeque<>();
    private final int maxSize = 5;

    public void addEvent(String event) {
        if (buffer.size() == maxSize) {
            buffer.removeFirst();
        }
        buffer.addLast(event);
    }

    public List<String> getReplayEvents() {
        return new ArrayList<>(buffer);
    }
}
