package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    //change to what ever id we use in User
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

}
