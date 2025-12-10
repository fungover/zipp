package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ReportEntity;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.profile.service.ProfileService;
import org.fungover.zipp.repository.ReportRepository;
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

    public ProfileController(ProfileService profileService,
                             ReportRepository reportRepository) {
        this.profileService = profileService;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/profilesettings")
    public String showProfile(Model model, Authentication authentication) {
        // Inloggad user
        User user = profileService.getCurrentUser(authentication);
        model.addAttribute("user", user);

        // Hämta alla rapporter för användarens email
        var reports = reportRepository.findAllBySubmittedBy_Email(user.getEmail());
        model.addAttribute("reports", reports);

        return "profilesettings";
    }

    @PostMapping("/profilesettings")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                Authentication authentication) {

        profileService.updateProfile(authentication, formUser);
        return "redirect:/profilesettings";
    }

    @PostMapping("/profilesettings/reports/{id}/delete")
    public String deleteReport(@PathVariable("id") Long id,
                               Authentication authentication) {

        User currentUser = profileService.getCurrentUser(authentication);

        ReportEntity report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));

        // Säkerhet: bara ägaren (eller ev. admin) får ta bort
        if (!report.getSubmittedBy().getId().equals(currentUser.getId())) {
            // här kan du kasta AccessDeniedException om du vill
            throw new RuntimeException("Not allowed to delete this report");
        }

        reportRepository.delete(report);

        // Ladda om sidan så listan uppdateras
        return "redirect:/profilesettings";
    }
}
