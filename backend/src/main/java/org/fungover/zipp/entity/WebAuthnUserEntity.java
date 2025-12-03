package org.fungover.zipp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "webauthn_user_entity")
public class WebAuthnUserEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 64)
    private byte[] userId;

    @Column
    private String displayName;

    protected WebAuthnUserEntity() {
    }

    public WebAuthnUserEntity(String username, byte[] userId, String displayName) {
        this.username = username;
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
