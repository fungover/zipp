/*package org.fungover.zipp.profile.service;

import org.fungover.zipp.profile.dto.UserDTO;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateProfile(UserDTO updatedUser) {

        User user = userRepository.findById(updatedUser.id())
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBio(updatedUser.bio());
        user.setCity(updatedUser.city());
        user.setDisplayName(updatedUser.displayName());
        System.out.println(user.getCity() + user.getBio() + user.getDisplayName());

        return userRepository.save(user);
    }

    public User getCurrentUser() {
        // Hämta den inloggades email från SecurityContext
        String email = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName(); // Google & Spring Security använder email som principal

        // Slå upp användaren i databasen
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}

*/
