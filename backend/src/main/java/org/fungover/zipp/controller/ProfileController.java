package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.service.ProfileService;
import org.fungover.zipp.repository.ReportRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final ReportRepository reportRepository;

    public ProfileController(ProfileService profileService, ReportRepository reportRepository) {
        this.profileService = profileService;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/profilesettings")
    public String showProfile(Model model, Authentication authentication) {
        boolean loggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", loggedIn);

        User user = profileService.getCurrentUser(authentication);
        model.addAttribute("user", user);

        var reports = reportRepository.findAllBySubmittedByEmail(user.getEmail());
        model.addAttribute("reports", reports);

        return "profilesettings";
    }

    @PostMapping("/profilesettings")
    public String updateProfile(@ModelAttribute("user") User formUser, Authentication authentication) {

        profileService.updateProfile(authentication, formUser);
        return "redirect:/profilesettings";
    }

    @PostMapping("/profilesettings/reports/{id}/delete")
    public String deleteReport(@PathVariable("id") Long id, Authentication authentication) {

        User currentUser = profileService.getCurrentUser(authentication);

        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));

        if (!report.getSubmittedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not allowed to delete this report");

        }

        reportRepository.delete(report);

        return "redirect:/profilesettings";
    }
}
