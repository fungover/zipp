package org.fungover.zipp.controller;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.dto.ReportResponse;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.service.ReportService;
import org.fungover.zipp.service.UserIdentityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReportControllerTest {

    private ReportController controller;
    private ReportService reportService;
    private KafkaTemplate<String, ReportResponse> kafkaTemplate;
    private UserIdentityService userIdentityService;

    private Authentication authentication;
    private User currentUser;

    @BeforeEach
    void setup() {
        reportService = mock(ReportService.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        userIdentityService = mock(UserIdentityService.class);

        controller = new ReportController(reportService, kafkaTemplate, userIdentityService);

        authentication = mock(Authentication.class);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("test@example.com");
        currentUser.setProviderId("provider-123");

        when(userIdentityService.getCurrentUser(authentication)).thenReturn(currentUser);

        CompletableFuture<SendResult<String, ReportResponse>> future = new CompletableFuture<>();
        future.complete(null);
        when(kafkaTemplate.send(eq("report"), any(ReportResponse.class))).thenReturn(future);
    }

    @Test
    void createReportShouldReturnCreatedReportWithStatus201() {
        User attacker = new User();
        attacker.setId(UUID.randomUUID());
        attacker.setEmail("attacker@example.com");
        attacker.setProviderId("attacker-999");

        Report inputReport = new Report( "Test description", ReportType.ACCIDENT, 59.3293, 18.0686, null);

        ReportResponse saved = new ReportResponse(currentUser.getProviderId(), "Test description", ReportType.ACCIDENT,
                59.3293, 18.0686, Instant.now(), ReportStatus.ACTIVE, List.of());

        when(reportService.createReport(eq(currentUser), eq(inputReport))).thenReturn(saved);

        ResponseEntity<ReportResponse> response = controller.createReport(inputReport, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());

        verify(userIdentityService).getCurrentUser(authentication);
        verify(reportService).createReport(currentUser, inputReport);
        verify(kafkaTemplate).send(eq("report"), eq(saved));
    }

    @Test
    void createReportWithImagesShouldReturnCreatedReport() {
        User attacker = new User();
        attacker.setId(UUID.randomUUID());
        attacker.setEmail("attacker@example.com");
        attacker.setProviderId("attacker-999");

        Report inputReport = new Report( "Report with images", ReportType.DEBRIS, 59.3293, 18.0686, List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

        ReportResponse saved = new ReportResponse(currentUser.getProviderId(), "Report with images", ReportType.DEBRIS,
                59.3293, 18.0686, Instant.now(), ReportStatus.ACTIVE,
                List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

        when(reportService.createReport(eq(currentUser), eq(inputReport))).thenReturn(saved);

        ResponseEntity<ReportResponse> response = controller.createReport(inputReport, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().imageUrls().size());

        verify(userIdentityService).getCurrentUser(authentication);
        verify(reportService).createReport(currentUser, inputReport);
        verify(kafkaTemplate).send(eq("report"), eq(saved));
    }

    @Test
    void getAllReportsShouldReturnAllActiveReports() {
        List<ReportResponse> expected = List.of(
                new ReportResponse("provider-123", "Report 1", ReportType.OTHER, 59.3293, 18.0686, Instant.now(),
                        ReportStatus.ACTIVE, List.of()),
                new ReportResponse("provider-456", "Report 2", ReportType.OTHER, 59.3294, 18.0687, Instant.now(),
                        ReportStatus.ACTIVE, List.of()));

        when(reportService.getAllReports()).thenReturn(expected);

        ResponseEntity<List<ReportResponse>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(reportService).getAllReports();
        verifyNoInteractions(userIdentityService);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void getAllReportsShouldReturnEmptyListWhenNoReportsExist() {
        when(reportService.getAllReports()).thenReturn(List.of());

        ResponseEntity<List<ReportResponse>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(reportService).getAllReports();
        verifyNoInteractions(userIdentityService);
        verifyNoInteractions(kafkaTemplate);
    }
}
