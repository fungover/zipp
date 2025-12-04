package org.fungover.zipp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"reports-topic"})
@AutoConfigureMockMvc
class EventIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void kafkaEventShouldReachSseClient() throws Exception {

        // Send event to kafka
        String json = "{\"id\":\"id123\",\"payload\":\"Hello SSE\",\"type\":\"report\",\"timestamp\":\"" + Instant.now() + "\"}";
        kafkaTemplate.send("reports-topic", json);


        // Open SSE-connection toward /events
        var result = mockMvc.perform(get("/events"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Read response
        String response = result.getResponse().getContentAsString();

        // Check if event is in SSE-stream
        assertTrue(response.contains("Hello SSE"));
    }
}
