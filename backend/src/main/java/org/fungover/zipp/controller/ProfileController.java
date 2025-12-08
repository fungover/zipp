package org.fungover.zipp.controller;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.profile.service.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profilesettings")
    public String showProfile(Model model, Authentication authentication) {
        User user = profileService.getCurrentUser(authentication);
        model.addAttribute("user", user);
        return "profilesettings";
    }

    @PostMapping("/profilesettings")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                Authentication authentication) {

        profileService.updateProfile(authentication, formUser);
        return "redirect:/profilesettings";
    }
}
