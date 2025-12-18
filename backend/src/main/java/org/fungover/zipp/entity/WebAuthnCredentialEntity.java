package org.fungover.zipp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Locale;

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

    @Column
    private String label;

    protected WebAuthnCredentialEntity() {
    }

    public WebAuthnCredentialEntity(byte[] credentialId, User user, byte[] publicKey, long signatureCount,
            String transports, byte[] attestationObject, byte[] clientDataJSON, String label) {
        this.credentialId = copy(credentialId);
        this.user = user;
        this.publicKey = copy(publicKey);
        this.signatureCount = signatureCount;
        this.transports = transports;
        this.attestationObject = copy(attestationObject);
        this.clientDataJSON = copy(clientDataJSON);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEncodedId() {
        try {
            if (this.credentialId == null)
                return "error";
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(this.credentialId);
        } catch (Exception e) {
            return "error";
        }
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

    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
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
                .map(s -> AuthenticatorTransport.valueOf(s.toUpperCase(Locale.ROOT))).collect(Collectors.toSet());
    }

    public static String transportsToString(Set<AuthenticatorTransport> transports) {
        if (transports == null || transports.isEmpty()) {
            return "";
        }

        return transports.stream().map(AuthenticatorTransport::getValue).sorted().collect(Collectors.joining(","));
    }

    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    private static byte[] copy(byte[] in) {
        if (in == null) {
            return null;
        }
        return Arrays.copyOf(in, in.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebAuthnCredentialEntity other)) {
            return false;
        }
        return Arrays.equals(this.credentialId, other.credentialId);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(credentialId);
    }
}
