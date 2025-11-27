package org.fungover.zipp.Profile.controller;

import org.fungover.zipp.Profile.service.ProfileService;
import org.springframework.stereotype.Controller;

@Controller
public class ProfilePageController {

    private final ProfileService profileService;

    public ProfilePageController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // routes
}
