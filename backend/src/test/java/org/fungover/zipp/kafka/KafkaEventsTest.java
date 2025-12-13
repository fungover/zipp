package org.fungover.zipp.kafka;

import org.fungover.zipp.TestLogAppender;
import org.fungover.zipp.controller.ReportController;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.core.Authentication;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import static org.fungover.zipp.dto.ReportStatus.ACTIVE;
import static org.fungover.zipp.dto.ReportType.OTHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaEventsTest {

    @InjectMocks
    private ReportController reportController;

    @Mock
    private KafkaTemplate<String, ReportResponse> kafkaTemplate;

    @Mock
    private ReportService reportService;

    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private Authentication authentication;

    @Test
    void kafkaReportSentSuccessfully() {
        ReportResponse savedReport = new ReportResponse("user1", "test report", OTHER, 0.0, 0.0, Instant.now(), ACTIVE,
                null);

        when(kafkaTemplate.send(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        reportController.sendReport("report", savedReport);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReportResponse> reportCaptor = ArgumentCaptor.forClass(ReportResponse.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), reportCaptor.capture());

        assertEquals("report", topicCaptor.getValue());
        assertEquals(savedReport, reportCaptor.getValue());
    }

    @Test
    void kafkaReportFailedToSendGivesError() {
        ReportResponse incomingReport = new ReportResponse("user1", "test report", OTHER, 0.0, 0.0, Instant.now(),
                ACTIVE, null);

        CompletableFuture<SendResult<String, ReportResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException());
        when(kafkaTemplate.send(anyString(), any())).thenReturn(failedFuture);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(ReportController.class);

        TestLogAppender appender = new TestLogAppender();
        logger.addAppender(appender);
        appender.start();

        reportController.sendReport("report", incomingReport);

        boolean hasError = appender.getLogs().stream()
                .anyMatch(event -> event.getLevel() == ch.qos.logback.classic.Level.ERROR
                        && event.getFormattedMessage().contains("Failed to publish report"));

        assertTrue(hasError);
    }
}
