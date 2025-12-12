package org.fungover.zipp.entity;

import jakarta.persistence.*;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredentialEntity {

    @Id
    @Column(name = "credential_id", nullable = false)
    private byte[] credentialId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private byte[] publicKey;

    @Column(nullable = false)
    private long signatureCount;

    @Column
    private String transports;

    @Lob
    private byte[] attestationObject;

    @Lob
    private byte[] clientDataJSON;

    protected WebAuthnCredentialEntity() {

    }

    public WebAuthnCredentialEntity(byte[] credentialId, User user, byte[] publicKey, long signatureCount,
            String transports, byte[] attestationObject, byte[] clientDataJSON) {
        this.credentialId = copy(credentialId);
        this.user = user;
        this.publicKey = copy(publicKey);
        this.signatureCount = signatureCount;
        this.transports = transports;
        this.attestationObject = copy(attestationObject);
        this.clientDataJSON = copy(clientDataJSON);
    }

    public byte[] getCredentialId() {
        return copy(credentialId);
    }

    public User getUser() {
        return user;
    }

    public byte[] getPublicKey() {
        return copy(publicKey);
    }

    public long getSignatureCount() {
        return signatureCount;
    }

    public String getTransports() {
        return transports;
    }

    public byte[] getAttestationObject() {
        return copy(attestationObject);
    }

    public byte[] getClientDataJSON() {
        return copy(clientDataJSON);
    }

    public Set<AuthenticatorTransport> getTransportsAsSet() {
        if (transports == null || transports.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(transports.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(AuthenticatorTransport::valueOf).collect(Collectors.toSet());
    }

    public static String transportsToString(Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return null;
        }
        return transports.stream().map(AuthenticatorTransport::getValue).sorted().collect(Collectors.joining(","));
    }

    private static byte[] copy(byte[] in) {
        if (in == null) {
            return null;
        }
        return Arrays.copyOf(in, in.length);
    }
}
