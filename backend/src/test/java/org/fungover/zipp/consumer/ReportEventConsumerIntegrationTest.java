package org.fungover.zipp.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"reports"})
public class ReportEventConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    @Test
    void testKafkaFlow() throws Exception {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);

        String testMessage = "Hello from EmbeddedKafka";
        template.send("reports", testMessage);

        boolean messageConsumed = latch.await(45, TimeUnit.SECONDS);

        assertThat(messageConsumed).isTrue();
        assertThat(receivedMessage).isEqualTo(testMessage);
    }

    @KafkaListener(topics = "reports", groupId = "test-group")
    public void testConsue(String message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}
