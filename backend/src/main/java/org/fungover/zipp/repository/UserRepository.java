package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmail(String email);
}
