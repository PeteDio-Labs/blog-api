package com.petedillo.api.repository;

import com.petedillo.api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entities.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its hash.
     *
     * @param tokenHash the SHA-256 hash of the token
     * @return an Optional containing the token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find all refresh tokens for a specific admin user.
     *
     * @param adminUserId the admin user ID
     * @return a list of refresh tokens for the user
     */
    List<RefreshToken> findByAdminUserId(Long adminUserId);

    /**
     * Find all refresh tokens in a token family (for reuse detection).
     *
     * @param tokenFamilyId the token family ID (UUID)
     * @return a list of tokens in the family
     */
    List<RefreshToken> findByTokenFamilyId(UUID tokenFamilyId);

    /**
     * Find all non-revoked refresh tokens for a user that haven't expired.
     *
     * @param adminUserId the admin user ID
     * @param now the current time
     * @return a list of valid refresh tokens
     */
    List<RefreshToken> findByAdminUserIdAndIsRevokedFalseAndExpiresAtAfter(Long adminUserId, LocalDateTime now);

    /**
     * Find all non-revoked tokens in a family.
     *
     * @param tokenFamilyId the token family ID
     * @return a list of non-revoked tokens in the family
     */
    List<RefreshToken> findByTokenFamilyIdAndIsRevokedFalse(UUID tokenFamilyId);
}
