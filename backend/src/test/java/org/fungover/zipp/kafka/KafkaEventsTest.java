package org.fungover.zipp.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.fungover.zipp.controller.ReportController;
import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static javax.management.Query.eq;
import static org.fungover.zipp.dto.ReportStatus.ACTIVE;
import static org.fungover.zipp.dto.ReportType.OTHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        ReportResponse savedReport = new ReportResponse("user1", "test report", OTHER, 0.0, 0.0, Instant.now(), ACTIVE, null);

        when(kafkaTemplate.send(anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        reportController.sendReport("report", savedReport);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReportResponse> reportCaptor = ArgumentCaptor.forClass(ReportResponse.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), reportCaptor.capture());

        assertEquals("report", topicCaptor.getValue());
        assertEquals(savedReport, reportCaptor.getValue());
    }

    @Test
    void kafkaReportFailedToSendGivesError(){

    }
}
