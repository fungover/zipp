package org.fungover.zipp.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"reports"})
public class ReportEventConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void testKafkaFlow() throws Exception {

        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);

        String testMessage = "Hello from EmbeddedKafka";
        template.send("reports", testMessage);

        Thread.sleep(500);
    }
}
