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

    @Test
    void bufferShouldNotExceedMaxSize() {
        ReplayService replayService = new ReplayService();
        for (int i = 1; i <= 10; i++) {
            replayService.addEvent("event" + i);
        }
        List<String> replayed = replayService.getReplayEvents();
        assertEquals(5, replayed.size());
        assertTrue(replayed.contains("event6"));
        assertTrue(replayed.contains("event10"));
    }

    @Test
    void replayShouldReturnEmptyListWhenNoEventAdded() {
        ReplayService replayService = new ReplayService();
        List<String> replayed = replayService.getReplayEvents();
        assertTrue(replayed.isEmpty());
    }

    @Test
    void bufferShouldHoldExactlyMaxSize() {
        ReplayService replayService = new ReplayService();
        for (int i = 1; i <= 5; i++) {
            replayService.addEvent("event" + i);
        }
        List<String> replayed = replayService.getReplayEvents();
        assertEquals(5, replayed.size());
        assertTrue(replayed.contains("event1"));
        assertTrue(replayed.contains("event5"));
    }

    @Test
    void replayShouldReturnEventsInOrder() {
        ReplayService replayService = new ReplayService();
        replayService.addEvent("event1");
        replayService.addEvent("event2");
        replayService.addEvent("event3");

        List<String> replayed = replayService.getReplayEvents();
        assertEquals(List.of("event1", "event2", "event3"), replayed);
    }
}
