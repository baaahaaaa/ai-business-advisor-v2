package com.aibusinessadvisor.backend.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;


    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;


    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;


    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 32
    )
    private UserRole role;


    @Column(nullable = false)
    private boolean enabled = true;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


    @Column(name = "last_login_at")
    private Instant lastLoginAt;


    protected AppUser() {
    }


    public AppUser(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            UserRole role,
            boolean enabled
    ) {

        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.enabled = enabled;
    }


    @PrePersist
    void prePersist() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }


    public UUID getId() {
        return id;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getPasswordHash() {
        return passwordHash;
    }


    public void setPasswordHash(
            String passwordHash
    ) {
        this.passwordHash = passwordHash;
    }


    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(
            String firstName
    ) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName(
            String lastName
    ) {
        this.lastName = lastName;
    }


    public UserRole getRole() {
        return role;
    }


    public void setRole(
            UserRole role
    ) {
        this.role = role;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Instant getLastLoginAt() {
        return lastLoginAt;
    }


    public void setLastLoginAt(
            Instant lastLoginAt
    ) {
        this.lastLoginAt = lastLoginAt;
    }
}