package org.fungover.zipp.repository;

import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebAuthnCredentialEntityRepository extends JpaRepository<WebAuthnCredentialEntity, byte[]> {

    List<WebAuthnCredentialEntity> findAllByUser_Id(UUID userId);
}
