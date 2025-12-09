package com.petedillo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JWT refresh token with rotation tracking for security.
 * Implements token family tracking to detect token reuse attacks.
 */
@Getter
@Setter
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_tokens_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_tokens_user", columnList = "admin_user_id"),
        @Index(name = "idx_refresh_tokens_family", columnList = "token_family_id"),
        @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
    }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenHash; // SHA-256 hash of the actual refresh token

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "admin_user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_refresh_tokens_admin_user")
    )
    private AdminUser adminUser;

    @Column(nullable = false)
    private UUID tokenFamilyId; // UUID linking rotated tokens for reuse detection

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = true)
    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private Boolean isRevoked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "user_agent", nullable = true)
    private String userAgent;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;
}
