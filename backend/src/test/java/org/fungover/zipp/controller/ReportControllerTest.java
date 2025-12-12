package org.fungover.zipp.controller;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.fungover.zipp.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private ReportController controller;
    private ReportService reportService;
    private UserRepository userRepository;

    private User dbUser;
    private Authentication authentication;

    @BeforeEach
    void setup() {
        reportService = mock(ReportService.class);
        userRepository = mock(UserRepository.class);
        controller = new ReportController(reportService, userRepository);

        dbUser = new User();
        dbUser.setId(UUID.randomUUID());
        dbUser.setEmail("test@example.com");
        dbUser.setProviderId("provider-123");

        authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("provider-123");
        when(authentication.getPrincipal()).thenReturn(principal);

        when(userRepository.findByProviderId("provider-123")).thenReturn(Optional.of(dbUser));
    }

    @Test
    void createReportShouldReturnCreatedReportWithStatus201() {
        User attacker = new User();
        attacker.setId(UUID.randomUUID());
        attacker.setEmail("attacker@example.com");

        Report inputReport = new Report(attacker, "Test description", ReportType.ACCIDENT, 59.3293, 18.0686, null, null,
                null);

        Report savedReport = new Report(dbUser, "Test description", ReportType.ACCIDENT, 59.3293, 18.0686,
                Instant.now(), ReportStatus.ACTIVE, List.of());

        when(reportService.createReport(eq(dbUser), eq(inputReport))).thenReturn(savedReport);

        ResponseEntity<Report> response = controller.createReport(inputReport, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedReport, response.getBody());

        verify(reportService).createReport(dbUser, inputReport);
        verify(userRepository).findByProviderId("provider-123");
    }

    @Test
    void createReportWithImagesShouldReturnCreatedReport() {
        User attacker = new User();
        attacker.setId(UUID.randomUUID());
        attacker.setEmail("attacker@example.com");

        Report inputReport = new Report(attacker, "Report with images", ReportType.DEBRIS, 59.3293, 18.0686, null, null,
                List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

        Report savedReport = new Report(dbUser, "Report with images", ReportType.DEBRIS, 59.3293, 18.0686,
                Instant.now(), ReportStatus.ACTIVE,
                List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

        when(reportService.createReport(eq(dbUser), eq(inputReport))).thenReturn(savedReport);

        ResponseEntity<Report> response = controller.createReport(inputReport, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().imageUrls().size());

        verify(reportService).createReport(dbUser, inputReport);
        verify(userRepository).findByProviderId("provider-123");
    }

    @Test
    void getAllReportsShouldReturnAllActiveReports() {
        List<Report> expectedReports = List.of(
                new Report(dbUser, "Report 1", ReportType.OTHER, 59.3293, 18.0686, Instant.now(), ReportStatus.ACTIVE,
                        List.of()),
                new Report(dbUser, "Report 2", ReportType.OTHER, 59.3294, 18.0687, Instant.now(), ReportStatus.ACTIVE,
                        List.of()));

        when(reportService.getAllReports()).thenReturn(expectedReports);

        ResponseEntity<List<Report>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReports, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(reportService).getAllReports();
    }

    @Test
    void getAllReportsShouldReturnEmptyListWhenNoReportsExist() {
        when(reportService.getAllReports()).thenReturn(List.of());

        ResponseEntity<List<Report>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(reportService).getAllReports();
    }

    @Test
    void getAllReportsForUserShouldReturnReportsForSpecificUser() {
        String userEmail = "user@example.com";

        List<Report> expectedReports = List.of(
                new Report(dbUser, "User report 1", ReportType.ACCIDENT, 59.3293, 18.0686, Instant.now(),
                        ReportStatus.ACTIVE, List.of()),
                new Report(dbUser, "User report 2", ReportType.ACCIDENT, 59.3294, 18.0687, Instant.now(),
                        ReportStatus.ACTIVE, List.of()));

        when(reportService.getAllReportsForUser(userEmail)).thenReturn(expectedReports);

        ResponseEntity<List<Report>> response = controller.getAllReportsForUser(userEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReports, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(reportService).getAllReportsForUser(userEmail);
    }

    @Test
    void getAllReportsForUserShouldReturnEmptyListWhenUserHasNoReports() {
        String userEmail = "no-reports-user@example.com";

        when(reportService.getAllReportsForUser(userEmail)).thenReturn(List.of());

        ResponseEntity<List<Report>> response = controller.getAllReportsForUser(userEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(reportService).getAllReportsForUser(userEmail);
    }
}
