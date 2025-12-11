package org.fungover.zipp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredentialEntity {

    @Id
    @Column(name = "credential_id", nullable = false)
    private byte[] credentialId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // FK -> users.id (UUID)
    private User user;

    @Lob
    @Column(nullable = false)
    private byte[] publicKey;

    @Column(nullable = false)
    private long signatureCount;

    @Column
    private String transports; // komma-separerad lista tex "internal,hybrid"

    @Lob
    private byte[] attestationObject;

    @Lob
    private byte[] clientDataJSON;

    protected WebAuthnCredentialEntity() {
    }

    public WebAuthnCredentialEntity(byte[] credentialId, User user, byte[] publicKey, long signatureCount,
            String transports, byte[] attestationObject, byte[] clientDataJSON) {
        this.credentialId = credentialId;
        this.user = user;
        this.publicKey = publicKey;
        this.signatureCount = signatureCount;
        this.transports = transports;
        this.attestationObject = attestationObject;
        this.clientDataJSON = clientDataJSON;
    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public User getUser() {
        return user;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public long getSignatureCount() {
        return signatureCount;
    }

    public String getTransports() {
        return transports;
    }

    public byte[] getAttestationObject() {
        return attestationObject;
    }

    public byte[] getClientDataJSON() {
        return clientDataJSON;
    }

    public Set<AuthenticatorTransport> getTransportsAsSet() {
        if (transports == null || transports.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(transports.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(AuthenticatorTransport::valueOf).collect(Collectors.toSet());
    }

    public static String transportsToString(Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return null;
        }
        return transports.stream().map(AuthenticatorTransport::getValue) // "internal", "usb", "hybrid" etc
                .sorted().collect(Collectors.joining(","));
    }
}
