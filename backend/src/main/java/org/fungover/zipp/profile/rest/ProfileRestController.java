package org.fungover.zipp.profile.rest;

import org.fungover.zipp.profile.service.ProfileService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileRestController {

    private final ProfileService profileService;

    public ProfileRestController(ProfileService profileService) {
        this.profileService = profileService;
    }

    //Controller for CRUD operations
/*    @GetMapping
    public List<User> findAll() {

    }*/
}
