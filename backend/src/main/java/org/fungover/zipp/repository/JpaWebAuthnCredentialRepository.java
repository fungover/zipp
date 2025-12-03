package org.fungover.zipp.repository;

import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.fungover.zipp.entity.WebAuthnUserEntity;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaWebAuthnCredentialRepository implements UserCredentialRepository {

    private final WebAuthnCredentialEntityRepository credRepo;
    private final WebAuthnUserEntityRepository userRepo;

    public JpaWebAuthnCredentialRepository(
        WebAuthnCredentialEntityRepository credRepo,
        WebAuthnUserEntityRepository userRepo
    ) {
        this.credRepo = credRepo;
        this.userRepo = userRepo;
    }

    private CredentialRecord mapToRecord(WebAuthnCredentialEntity c) {
        return ImmutableCredentialRecord.builder()
            .credentialId(new Bytes(c.getCredentialId()))
            .userEntityUserId(new Bytes(c.getUser().getUserId()))
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

    private WebAuthnCredentialEntity mapToEntity(CredentialRecord record, WebAuthnUserEntity user) {
        byte[] credentialId = record.getCredentialId().getBytes();
        byte[] userId = record.getUserEntityUserId().getBytes();

        if (!java.util.Arrays.equals(user.getUserId(), userId)) {
            throw new IllegalStateException("UserId mismatch mellan CredentialRecord och WebAuthnUserEntity");
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
        return credRepo.findAllByUser_UserId(userId.getBytes())
            .stream()
            .map(this::mapToRecord)
            .collect(Collectors.toList());
    }

    @Override
    public void save(CredentialRecord credentialRecord) {
        WebAuthnUserEntity user = userRepo.findByUserId(
                credentialRecord.getUserEntityUserId().getBytes()
            )
            .orElseThrow(() -> new IllegalStateException("User not found"));

        WebAuthnCredentialEntity entity = mapToEntity(credentialRecord, user);
        credRepo.save(entity);
    }

    @Override
    public void delete(Bytes credentialId) {
        credRepo.deleteById(credentialId.getBytes());
    }
}
