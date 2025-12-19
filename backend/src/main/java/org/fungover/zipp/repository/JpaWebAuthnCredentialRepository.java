package org.fungover.zipp.repository;

import jakarta.servlet.http.HttpServletRequest;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.fungover.zipp.util.WebAuthnUuidUtils.bytesToUuid;
import static org.fungover.zipp.util.WebAuthnUuidUtils.uuidToBytes;

@Component
public class JpaWebAuthnCredentialRepository implements UserCredentialRepository {

    private final WebAuthnCredentialEntityRepository credRepo;
    private final UserRepository userRepo;

    public JpaWebAuthnCredentialRepository(WebAuthnCredentialEntityRepository credRepo, UserRepository userRepo) {
        this.credRepo = credRepo;
        this.userRepo = userRepo;
    }

    private CredentialRecord mapToRecord(WebAuthnCredentialEntity c) {
        UUID userId = c.getUser().getId();

        return ImmutableCredentialRecord.builder().credentialId(new Bytes(c.getCredentialId()))
                .userEntityUserId(new Bytes(uuidToBytes(userId)))
                .publicKey(new ImmutablePublicKeyCose(c.getPublicKey())).signatureCount(c.getSignatureCount())
                .transports(c.getTransportsAsSet())
                .attestationObject(c.getAttestationObject() != null ? new Bytes(c.getAttestationObject()) : null)
                .attestationClientDataJSON(c.getClientDataJSON() != null ? new Bytes(c.getClientDataJSON()) : null)
                .build();
    }

    private WebAuthnCredentialEntity mapToEntity(CredentialRecord record, User user) {
        byte[] credentialId = record.getCredentialId().getBytes();
        UUID userIdFromRecord = bytesToUuid(record.getUserEntityUserId().getBytes());

        if (!user.getId().equals(userIdFromRecord)) {
            throw new IllegalStateException("UserId mismatch between CredentialRecord and User");
        }

        String transports = WebAuthnCredentialEntity.transportsToString(record.getTransports());

        byte[] attestationObject = record.getAttestationObject() != null
                ? record.getAttestationObject().getBytes()
                : null;

        byte[] clientDataJSON = record.getAttestationClientDataJSON() != null
                ? record.getAttestationClientDataJSON().getBytes()
                : null;

        String labelFromUrl = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String param = request.getParameter("label");
            if (param != null && !param.isBlank()) {
                labelFromUrl = param;
            }
        }

        return new WebAuthnCredentialEntity(credentialId, user, record.getPublicKey().getBytes(),
                record.getSignatureCount(), transports, attestationObject, clientDataJSON, labelFromUrl);
    }

    @Override
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        return credRepo.findById(credentialId.getBytes()).map(this::mapToRecord).orElse(null);
    }

    @Override
    public List<CredentialRecord> findByUserId(Bytes userId) {
        UUID uuid = bytesToUuid(userId.getBytes());
        return credRepo.findAllByUserId(uuid).stream().map(this::mapToRecord).collect(Collectors.toList());
    }

    @Override
    public void save(CredentialRecord credentialRecord) {
        UUID uuid = bytesToUuid(credentialRecord.getUserEntityUserId().getBytes());

        User user = userRepo.findById(uuid)
                .orElseThrow(() -> new IllegalStateException("User not found for WebAuthn credential"));

        var existingEntity = credRepo.findById(credentialRecord.getCredentialId().getBytes());

        WebAuthnCredentialEntity entity;

        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            entity.setSignatureCount(credentialRecord.getSignatureCount());
            entity.setTransports(WebAuthnCredentialEntity.transportsToString(credentialRecord.getTransports()));
        } else {
            entity = mapToEntity(credentialRecord, user);
        }

        credRepo.save(entity);
    }

    @Override
    public void delete(Bytes credentialId) {
        credRepo.deleteById(credentialId.getBytes());
    }
}
