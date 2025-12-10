package org.fungover.zipp.controller;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.profile.service.ProfileService;
import org.fungover.zipp.repository.ReportRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        // 1. Hämta inloggad user
        User user = profileService.getCurrentUser(authentication);
        model.addAttribute("user", user);

        // 2. Hämta alla rapporter för användarens email
        var reports = reportRepository.findAllBySubmittedBy_Email(user.getEmail());

        // 3. Lägg in i modellen så Thymeleaf kan använda ${reports}
        model.addAttribute("reports", reports);

        return "profilesettings";
    }

    @PostMapping("/profilesettings")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                Authentication authentication) {

        profileService.updateProfile(authentication, formUser);
        return "redirect:/profilesettings";
    }
}
