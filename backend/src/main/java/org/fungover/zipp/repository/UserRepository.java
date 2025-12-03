package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	// change to what ever id we use in User
	Optional<User> findByProviderAndProviderId(String provider, String providerId);

	List<User> findUserByEmail(String email);
}
