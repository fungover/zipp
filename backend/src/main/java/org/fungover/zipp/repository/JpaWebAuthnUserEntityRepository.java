package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.UUID;

@Component
public class JpaWebAuthnUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

    private final UserRepository userRepository;

    public JpaWebAuthnUserEntityRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // === helpers: UUID <-> byte[] ===

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long most = bb.getLong();
        long least = bb.getLong();
        return new UUID(most, least);
    }

    private PublicKeyCredentialUserEntity mapToUserEntity(User u) {
        String displayName = u.getDisplayName() != null && !u.getDisplayName().isBlank()
            ? u.getDisplayName()
            : u.getName();

        return ImmutablePublicKeyCredentialUserEntity.builder()
            // WebAuthn userHandle = UUID som bytes
            .id(new Bytes(uuidToBytes(u.getId())))
            // WebAuthn "name" = providerId (Google-ID:et, t.ex. "1034673...")
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
        // username = authentication.getName() = providerId vid Google OAuth
        return userRepository.findByProviderId(username)
            .map(this::mapToUserEntity)
            .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        // Vi skapar inte nya users här – de kommer från OAuth2-flödet.
        // Du kan ev. synca displayName om du vill.
        UUID uuid;
        try {
            uuid = bytesToUuid(userEntity.getId().getBytes());
        } catch (Exception ex) {
            // Om det inte går att tolka som UUID: skit i det.
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
        // Vi raderar inte riktiga users via WebAuthn.
        // Här gör vi ingenting.
    }
}
