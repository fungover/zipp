package org.fungover.zipp.repository;

import org.fungover.zipp.entity.WebAuthnUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebAuthnUserEntityRepository extends JpaRepository<WebAuthnUserEntity, String> {
    Optional<WebAuthnUserEntity> findByUserId(byte[] userId);
}
