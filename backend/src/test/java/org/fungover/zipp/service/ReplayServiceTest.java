package org.fungover.zipp.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayServiceTest {
    @Test
    void replayShouldReturnLastEvents() {
        ReplayService replayService = new ReplayService();

        replayService.addEvent("event1");
        replayService.addEvent("event2");

        List<String> replayed = replayService.getReplayEvents();

        assertEquals(2, replayed.size());
        assertTrue(replayed.contains("event1"));
        assertTrue(replayed.contains("event2"));
    }
}
