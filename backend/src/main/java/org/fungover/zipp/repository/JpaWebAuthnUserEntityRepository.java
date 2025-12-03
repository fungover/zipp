package org.fungover.zipp.repository;

import org.fungover.zipp.entity.WebAuthnUserEntity;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaWebAuthnUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

    private final WebAuthnUserEntityRepository repo;

    public JpaWebAuthnUserEntityRepository(WebAuthnUserEntityRepository repo) {
        this.repo = repo;
    }

    private PublicKeyCredentialUserEntity mapToUserEntity(WebAuthnUserEntity e) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
            .id(new Bytes(e.getUserId()))
            .name(e.getUsername())
            .displayName(e.getDisplayName())
            .build();
    }

    @Override
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        return repo.findByUserId(id.getBytes())
            .map(this::mapToUserEntity)
            .orElse(null);
    }

    @Override
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        return repo.findById(username)
            .map(this::mapToUserEntity)
            .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        WebAuthnUserEntity entity = new WebAuthnUserEntity(
            userEntity.getName(),
            userEntity.getId().getBytes(),
            userEntity.getDisplayName()
        );
        repo.save(entity);
    }

    @Override
    public void delete(Bytes id) {
        repo.findByUserId(id.getBytes()).ifPresent(repo::delete);
    }
}
