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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"reports"})
@Testcontainers
public class ReportEventConsumerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0").withDatabaseName("testdb")
            .withUsername("testuser").withPassword("testpass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

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

        boolean messageConsumed = latch.await(5, TimeUnit.SECONDS);

        assertThat(messageConsumed).isTrue();
        assertThat(receivedMessage).isEqualTo(testMessage);
    }

    @KafkaListener(topics = "reports", groupId = "test-group")
    public void testConsume(String message) {
        this.receivedMessage = message;
        latch.countDown();
    }
}
