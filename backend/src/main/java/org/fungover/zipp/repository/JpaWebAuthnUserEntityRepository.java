package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.fungover.zipp.util.WebAuthnUuidUtils.bytesToUuid;
import static org.fungover.zipp.util.WebAuthnUuidUtils.uuidToBytes;

@Component
public class JpaWebAuthnUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

    private final UserRepository userRepository;

    public JpaWebAuthnUserEntityRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private PublicKeyCredentialUserEntity mapToUserEntity(User u) {
        String displayName = u.getDisplayName() != null && !u.getDisplayName().isBlank()
            ? u.getDisplayName()
            : u.getName();

        return ImmutablePublicKeyCredentialUserEntity.builder()
            .id(new Bytes(uuidToBytes(u.getId())))
            .name(u.getProviderId())
            .displayName(displayName)
            .build();
    }

    @Override
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        UUID uuid = bytesToUuid(id.getBytes());
        return userRepository.findById(uuid)
            .map(this::mapToUserEntity)
            .orElse(null);
    }

    @Override
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        return userRepository.findByProviderId(username)
            .map(this::mapToUserEntity)
            .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        UUID uuid;
        try {
            uuid = bytesToUuid(userEntity.getId().getBytes());
        } catch (Exception ex) {
            return;
        }

        userRepository.findById(uuid).ifPresent(user -> {
            String displayName = userEntity.getDisplayName();
            if (displayName != null && !displayName.isBlank()
                && (user.getDisplayName() == null || !user.getDisplayName().equals(displayName))) {
                user.setDisplayName(displayName);
                userRepository.save(user);
            }
        });
    }

    @Override
    public void delete(Bytes id) {
    }
}
