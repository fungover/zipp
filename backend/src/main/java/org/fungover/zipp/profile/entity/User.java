package org.fungover.zipp.profile.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bio")
    private String bio;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public User() {}

    public User(Long id, String bio, String displayName, String googleId,
                String address, String city,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bio = bio;
        this.displayName = displayName;
        this.googleId = googleId;
        this.address = address;
        this.city = city;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // -------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
