package com.petedillo.api.repository;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.RefreshToken;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RefreshTokenRepository Tests")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    private AdminUser testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        testUser = adminUserRepository.save(TestDataFactory.createAdminUser());
        testToken = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("test_hash_" + UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("should save and retrieve refresh token by hash")
    void testSaveAndFindByHash() {
        // Arrange
        String tokenHash = "unique_hash_" + UUID.randomUUID();
        RefreshToken token = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash(tokenHash)
                .build();

        // Act
        RefreshToken saved = refreshTokenRepository.save(token);
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(tokenHash);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getAdminUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("should find all tokens for an admin user")
    void testFindByAdminUserId() {
        // Arrange
        refreshTokenRepository.save(testToken);
        RefreshToken token2 = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("another_hash_" + UUID.randomUUID())
                .build();
        refreshTokenRepository.save(token2);

        // Act
        List<RefreshToken> tokens = refreshTokenRepository.findByAdminUserId(testUser.getId());

        // Assert
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("should find tokens by token family id")
    void testFindByTokenFamilyId() {
        // Arrange
        UUID familyId = UUID.randomUUID();
        RefreshToken token1 = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("hash1_" + UUID.randomUUID())
                .tokenFamilyId(familyId)
                .build();
        RefreshToken token2 = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("hash2_" + UUID.randomUUID())
                .tokenFamilyId(familyId)
                .build();

        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);

        // Act
        List<RefreshToken> family = refreshTokenRepository.findByTokenFamilyId(familyId);

        // Assert
        assertThat(family).hasSize(2);
        assertThat(family).allMatch(t -> t.getTokenFamilyId().equals(familyId));
    }

    @Test
    @DisplayName("should find valid (non-revoked, non-expired) tokens for user")
    void testFindValidTokensForUser() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        RefreshToken validToken = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("valid_" + UUID.randomUUID())
                .expiresAt(now.plusDays(1))
                .isRevoked(false)
                .build();
        RefreshToken expiredToken = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("expired_" + UUID.randomUUID())
                .expiresAt(now.minusHours(1))
                .isRevoked(false)
                .build();
        RefreshToken revokedToken = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("revoked_" + UUID.randomUUID())
                .expiresAt(now.plusDays(1))
                .isRevoked(true)
                .build();

        refreshTokenRepository.save(validToken);
        refreshTokenRepository.save(expiredToken);
        refreshTokenRepository.save(revokedToken);

        // Act
        List<RefreshToken> valid = refreshTokenRepository
                .findByAdminUserIdAndIsRevokedFalseAndExpiresAtAfter(testUser.getId(), now);

        // Assert
        assertThat(valid).hasSize(1);
        assertThat(valid.get(0).getTokenHash()).isEqualTo("valid_" + validToken.getTokenHash().substring(6));
    }

    @Test
    @DisplayName("should find non-revoked tokens in family")
    void testFindNonRevokedTokensInFamily() {
        // Arrange
        UUID familyId = UUID.randomUUID();
        RefreshToken active = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("active_" + UUID.randomUUID())
                .tokenFamilyId(familyId)
                .isRevoked(false)
                .build();
        RefreshToken revoked = TestDataFactory.refreshTokenBuilder()
                .adminUser(testUser)
                .tokenHash("revoked_" + UUID.randomUUID())
                .tokenFamilyId(familyId)
                .isRevoked(true)
                .build();

        refreshTokenRepository.save(active);
        refreshTokenRepository.save(revoked);

        // Act
        List<RefreshToken> nonRevoked = refreshTokenRepository
                .findByTokenFamilyIdAndIsRevokedFalse(familyId);

        // Assert
        assertThat(nonRevoked).hasSize(1);
        assertThat(nonRevoked.get(0).getIsRevoked()).isFalse();
    }

    @Test
    @DisplayName("should update refresh token")
    void testUpdateRefreshToken() {
        // Arrange
        RefreshToken saved = refreshTokenRepository.save(testToken);
        Long tokenId = saved.getId();

        // Act
        saved.setIsRevoked(true);
        saved.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(saved);

        // Assert
        RefreshToken updated = refreshTokenRepository.findById(tokenId).orElseThrow();
        assertThat(updated.getIsRevoked()).isTrue();
        assertThat(updated.getRevokedAt()).isNotNull();
    }

    @Test
    @DisplayName("should delete refresh token")
    void testDeleteRefreshToken() {
        // Arrange
        RefreshToken saved = refreshTokenRepository.save(testToken);
        Long tokenId = saved.getId();

        // Act
        refreshTokenRepository.deleteById(tokenId);

        // Assert
        Optional<RefreshToken> deleted = refreshTokenRepository.findById(tokenId);
        assertThat(deleted).isEmpty();
    }
}
