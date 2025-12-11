package org.fungover.zipp.controller;

import org.fungover.zipp.dto.Report;
import org.fungover.zipp.entity.ReportStatus;
import org.fungover.zipp.entity.ReportType;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportControllerTest {

    private ReportController controller;
    private ReportService reportService;
    private User mockUser;

    @BeforeEach
    void setup() {
        reportService = mock(ReportService.class);
        controller = new ReportController(reportService);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
    }

    @Test
    void createReport_ShouldReturnCreatedReport_WithStatus201() {
        Report inputReport = new Report(
            mockUser,
            "Test description",
            ReportType.ACCIDENT,
            59.3293,
            18.0686,
            null,
            null,
            null
        );

        Report savedReport = new Report(
            mockUser,
            "Test description",
            ReportType.ACCIDENT,
            59.3293,
            18.0686,
            Instant.now(),
            ReportStatus.ACTIVE,
            List.of()
        );

        when(reportService.createReport(inputReport)).thenReturn(savedReport);

        ResponseEntity<Report> response = controller.createReport(inputReport);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedReport, response.getBody());
        verify(reportService).createReport(inputReport);
    }

    @Test
    void createReport_WithImages_ShouldReturnCreatedReport() {
        Report inputReport = new Report(
            mockUser,
            "Report with images",
            ReportType.DEBRIS,
            59.3293,
            18.0686,
            null,
            null,
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")
        );

        Report savedReport = new Report(
            mockUser,
            "Report with images",
            ReportType.DEBRIS,
            59.3293,
            18.0686,
            Instant.now(),
            ReportStatus.ACTIVE,
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")
        );

        when(reportService.createReport(inputReport)).thenReturn(savedReport);

        ResponseEntity<Report> response = controller.createReport(inputReport);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().imageUrls().size());
        verify(reportService).createReport(inputReport);
    }

    @Test
    void getAllReports_ShouldReturnAllActiveReports() {
        List<Report> expectedReports = List.of(
            new Report(mockUser, "Report 1", ReportType.OTHER, 59.3293, 18.0686,
                      Instant.now(), ReportStatus.ACTIVE, List.of()),
            new Report(mockUser, "Report 2", ReportType.OTHER, 59.3294, 18.0687,
                      Instant.now(), ReportStatus.ACTIVE, List.of())
        );

        when(reportService.getAllReports()).thenReturn(expectedReports);

        ResponseEntity<List<Report>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReports, response.getBody());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());
        verify(reportService).getAllReports();
    }

    @Test
    void getAllReports_ShouldReturnEmptyList_WhenNoReportsExist() {
        when(reportService.getAllReports()).thenReturn(List.of());

        ResponseEntity<List<Report>> response = controller.getAllReports();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(reportService).getAllReports();
    }

    @Test
    void getAllReportsForUser_ShouldReturnReportsForSpecificUser() {
        String userEmail = "user@example.com";

        List<Report> expectedReports = List.of(
            new Report(mockUser, "User report 1", ReportType.ACCIDENT, 59.3293, 18.0686,
                      Instant.now(), ReportStatus.ACTIVE, List.of()),
            new Report(mockUser, "User report 2", ReportType.ACCIDENT, 59.3294, 18.0687,
                      Instant.now(), ReportStatus.ACTIVE, List.of())
        );

        when(reportService.getAllReportsForUser(userEmail)).thenReturn(expectedReports);

        ResponseEntity<List<Report>> response = controller.getAllReports(userEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedReports, response.getBody());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());
        verify(reportService).getAllReportsForUser(userEmail);
    }

    @Test
    void getAllReportsForUser_ShouldReturnEmptyList_WhenUserHasNoReports() {
        String userEmail = "no-reports-user@example.com";

        when(reportService.getAllReportsForUser(userEmail)).thenReturn(List.of());

        ResponseEntity<List<Report>> response = controller.getAllReports(userEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(reportService).getAllReportsForUser(userEmail);
    }
}
