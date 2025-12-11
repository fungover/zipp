package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaWebAuthnCredentialRepository implements UserCredentialRepository {

    private final WebAuthnCredentialEntityRepository credRepo;
    private final UserRepository userRepo;

    public JpaWebAuthnCredentialRepository(
        WebAuthnCredentialEntityRepository credRepo,
        UserRepository userRepo
    ) {
        this.credRepo = credRepo;
        this.userRepo = userRepo;
    }


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

    private CredentialRecord mapToRecord(WebAuthnCredentialEntity c) {
        UUID userId = c.getUser().getId();

        return ImmutableCredentialRecord.builder()
            .credentialId(new Bytes(c.getCredentialId()))
            .userEntityUserId(new Bytes(uuidToBytes(userId)))
            .publicKey(new ImmutablePublicKeyCose(c.getPublicKey()))
            .signatureCount(c.getSignatureCount())
            .transports(c.getTransportsAsSet())
            .attestationObject(
                c.getAttestationObject() != null ? new Bytes(c.getAttestationObject()) : null
            )
            .attestationClientDataJSON(
                c.getClientDataJSON() != null ? new Bytes(c.getClientDataJSON()) : null
            )
            .build();
    }

    private WebAuthnCredentialEntity mapToEntity(CredentialRecord record, User user) {
        byte[] credentialId = record.getCredentialId().getBytes();
        UUID userIdFromRecord = bytesToUuid(record.getUserEntityUserId().getBytes());

        if (!user.getId().equals(userIdFromRecord)) {
            throw new IllegalStateException("UserId mismatch mellan CredentialRecord och User");
        }

        String transports = WebAuthnCredentialEntity.transportsToString(record.getTransports());

        byte[] attestationObject = record.getAttestationObject() != null
            ? record.getAttestationObject().getBytes()
            : null;

        byte[] clientDataJSON = record.getAttestationClientDataJSON() != null
            ? record.getAttestationClientDataJSON().getBytes()
            : null;

        return new WebAuthnCredentialEntity(
            credentialId,
            user,
            record.getPublicKey().getBytes(),
            record.getSignatureCount(),
            transports,
            attestationObject,
            clientDataJSON
        );
    }

    @Override
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        return credRepo.findById(credentialId.getBytes())
            .map(this::mapToRecord)
            .orElse(null);
    }

    @Override
    public List<CredentialRecord> findByUserId(Bytes userId) {
        UUID uuid = bytesToUuid(userId.getBytes());
        return credRepo.findAllByUser_Id(uuid)
            .stream()
            .map(this::mapToRecord)
            .collect(Collectors.toList());
    }

    @Override
    public void save(CredentialRecord credentialRecord) {
        UUID uuid = bytesToUuid(credentialRecord.getUserEntityUserId().getBytes());

        User user = userRepo.findById(uuid)
            .orElseThrow(() -> new IllegalStateException("User not found for WebAuthn credential"));

        WebAuthnCredentialEntity entity = mapToEntity(credentialRecord, user);
        credRepo.save(entity);
    }

    @Override
    public void delete(Bytes credentialId) {
        credRepo.deleteById(credentialId.getBytes());
    }
}
