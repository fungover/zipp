package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.service.ProfileService;
import org.fungover.zipp.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    private ProfileController controller;
    private ProfileService profileService;
    private ReportRepository reportRepository;
    private User mockUser;

    @BeforeEach
    void setup() {
        profileService = mock(ProfileService.class);
        reportRepository = mock(ReportRepository.class);
        controller = new ProfileController(profileService, reportRepository);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
    }

    @Test
    void showProfileShouldReturnProfileSettingsViewWithUserAndReports() {
        Authentication auth = mock(Authentication.class);
        Model model = mock(Model.class);

        when(profileService.getCurrentUser(any())).thenReturn(mockUser);
        when(reportRepository.findAllBySubmittedBy_Email("test@example.com")).thenReturn(List.of(new ReportEntity()));

        String viewName = controller.showProfile(model, auth);

        assertEquals("profilesettings", viewName);
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute(eq("reports"), any());
    }

    @Test
    void updateProfileShouldCallServiceAndRedirect() {
        Authentication auth = mock(Authentication.class);
        User formUser = new User();
        formUser.setEmail("new@example.com");

        String result = controller.updateProfile(formUser, auth);

        assertEquals("redirect:/profilesettings", result);
        verify(profileService).updateProfile(auth, formUser);
    }

    @Test
    void deleteReportShouldDeleteWhenUserOwnsReport() {
        Authentication auth = mock(Authentication.class);

        ReportEntity report = new ReportEntity();
        report.setId(10L);
        report.setSubmittedBy(mockUser);

        when(profileService.getCurrentUser(any())).thenReturn(mockUser);
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));

        String result = controller.deleteReport(10L, auth);

        assertEquals("redirect:/profilesettings", result);
        verify(reportRepository).delete(report);
    }

    @Test
    void deleteReportShouldThrowWhenUserIsNotOwner() {
        Authentication auth = mock(Authentication.class);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID()); // annan UUID

        ReportEntity report = new ReportEntity();
        report.setId(10L);
        report.setSubmittedBy(otherUser);

        when(profileService.getCurrentUser(any())).thenReturn(mockUser);
        when(reportRepository.findById(10L)).thenReturn(Optional.of(report));

        assertThrows(RuntimeException.class, () -> controller.deleteReport(10L, auth));

        verify(reportRepository, never()).delete(any());
    }

    @Test
    void deleteReportShouldFailWhenReportMissing() {
        Authentication auth = mock(Authentication.class);

        when(profileService.getCurrentUser(any())).thenReturn(mockUser);
        when(reportRepository.findById(666L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.deleteReport(666L, auth));
    }
}
