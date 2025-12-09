package com.petedillo.api.model;

import com.petedillo.api.model.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Admin user account supporting multiple authentication providers.
 * Supports both local password-based auth and OAuth-based auth (Google, Apple, etc).
 */
@Getter
@Setter
@Entity
@Table(
    name = "admin_users",
    indexes = {
        @Index(name = "idx_admin_users_email", columnList = "email"),
        @Index(name = "idx_admin_users_username", columnList = "username"),
        @Index(name = "idx_admin_users_provider", columnList = "auth_provider,provider_user_id")
    }
)
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash; // Nullable for OAuth-only users

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_user_id", nullable = true)
    private String providerUserId; // OAuth provider's user ID

    @Column(name = "display_name", nullable = true)
    private String displayName;

    @Column(name = "profile_picture_url", nullable = true)
    private String profilePictureUrl;

    @Column(nullable = false)
    private Boolean isEnabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login_at", nullable = true)
    private LocalDateTime lastLoginAt;
}
