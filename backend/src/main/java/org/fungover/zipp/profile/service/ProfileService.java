package org.fungover.zipp.profile.service;

import org.fungover.zipp.profile.entity.User;
import org.fungover.zipp.profile.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {
    private final  UserRepository userRepository;

    // fetch from autentichation

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

}
