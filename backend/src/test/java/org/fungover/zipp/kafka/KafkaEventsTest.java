package org.fungover.zipp.kafka;

import org.fungover.zipp.TestLogAppender;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.mapper.ReportDtoToAvroMapper;
import org.fungover.zipp.service.ReportEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import static org.fungover.zipp.entity.ReportStatus.ACTIVE;
import static org.fungover.zipp.entity.ReportType.OTHER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventsTest {

    @Mock
    KafkaTemplate<String, ReportAvro> kafkaTemplate;

    @Mock
    ReportDtoToAvroMapper mapper;

    @InjectMocks
    ReportEventPublisher reportEventPublisher;

    @Test
    void kafkaReportSentSuccessfully() {
        ReportResponse report = new ReportResponse("user1", "test report", OTHER, 0.0, 0.0, Instant.now(), ACTIVE,
                null);

        ReportAvro avro = new ReportAvro();

        when(mapper.toAvro(report)).thenReturn(avro);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        reportEventPublisher.publishReportCreated(report);

        verify(kafkaTemplate).send(any(), eq("user1"), eq(avro));
    }

    @Test
    void kafkaReportFailedToSendGivesError() {
        ReportResponse incomingReport = new ReportResponse("user1", "test report", OTHER, 0.0, 0.0, Instant.now(),
                ACTIVE, null);

        CompletableFuture<SendResult<String, ReportAvro>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka is down"));

        // Stubbing med matchers för alla parametrar
        when(kafkaTemplate.send(nullable(String.class), nullable(String.class), nullable(ReportAvro.class)))
                .thenReturn(failedFuture);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(ReportEventPublisher.class);

        TestLogAppender appender = new TestLogAppender();
        logger.addAppender(appender);
        appender.start();

        reportEventPublisher.publishReportCreated(incomingReport);

        boolean hasError = appender.getLogs().stream()
                .anyMatch(event -> event.getLevel() == ch.qos.logback.classic.Level.ERROR
                        && event.getFormattedMessage().contains("Failed to publish report"));

        assertTrue(hasError);
    }
}
