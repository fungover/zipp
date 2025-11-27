package org.fungover.zipp.profile.controller;

import org.fungover.zipp.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfilePageController {

    private final ProfileService profileService;

    public ProfilePageController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // routes
    @GetMapping("/user")
    public ResponseEntity<?> profilePage() {
        return ResponseEntity.ok().body("User");
    }
}

