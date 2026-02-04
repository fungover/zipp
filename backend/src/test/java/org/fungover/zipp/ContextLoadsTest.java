package org.fungover.zipp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
class ContextLoadsTest {

    @Test
    void contextLoads() {
        // This is just a test-function
    }
}

