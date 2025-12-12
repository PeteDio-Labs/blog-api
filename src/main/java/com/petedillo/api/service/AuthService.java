package com.petedillo.api.service;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.RefreshToken;
import com.petedillo.api.repository.AdminUserRepository;
import com.petedillo.api.repository.RefreshTokenRepository;
import com.petedillo.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for authentication operations including login, logout, and token refresh.
 */
@Service
@Transactional
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminUserRepository adminUserRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      JwtTokenProvider tokenProvider,
                      PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate user with username and password.
     *
     * @param username the username
     * @param password the password
     * @return the authenticated AdminUser if credentials are valid
     */
    public Optional<AdminUser> authenticateUser(String username, String password) {
        return adminUserRepository.findByUsername(username)
                .filter(AdminUser::getIsEnabled)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

/**
 * Generate tokens for an authenticated user and store refresh token.
 *
 * @param adminUser the authenticated user
 * @param userAgent the user agent string
 * @param ipAddress the client IP address
 * @return pair of [accessToken, refreshToken]
 */
public String[] generateTokens(AdminUser adminUser, String userAgent, String ipAddress) {
    String accessToken = tokenProvider.generateAccessToken(adminUser);
    String refreshTokenValue = tokenProvider.generateRefreshToken(adminUser);

    // Hash the refresh token before storing
    String tokenHash = hashToken(refreshTokenValue);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setAdminUser(adminUser);
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setTokenFamilyId(UUID.randomUUID());
    refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
    refreshToken.setIsRevoked(false);
    refreshToken.setUserAgent(userAgent);
    refreshToken.setIpAddress(ipAddress);

    refreshTokenRepository.save(refreshToken);

    return new String[]{accessToken, refreshTokenValue};
}    /**
     * Validate and refresh tokens using a refresh token.
     *
     * @param refreshTokenValue the refresh token value
     * @return new access token if refresh is successful
     */
    public Optional<String> refreshAccessToken(String refreshTokenValue) {
        try {
            String tokenHash = hashToken(refreshTokenValue);
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByTokenHash(tokenHash);

            if (refreshToken.isEmpty() || refreshToken.get().getIsRevoked()) {
                return Optional.empty();
            }

            if (refreshToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                return Optional.empty();
            }

            AdminUser user = refreshToken.get().getAdminUser();
            String newAccessToken = tokenProvider.generateAccessToken(user);

            return Optional.of(newAccessToken);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Revoke all refresh tokens for a user (logout).
     *
     * @param adminUserId the user ID
     */
    public void revokeUserTokens(Long adminUserId) {
        refreshTokenRepository.findByAdminUserId(adminUserId).forEach(token -> {
            token.setIsRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    /**
     * SHA-256 hash function for refresh tokens.
     * Provides secure, collision-resistant hashing for token storage.
     *
     * @param token the token to hash
     * @return SHA-256 hashed token
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Get user by ID.
     *
     * @param userId the user ID
     * @return the AdminUser if found
     */
    public Optional<AdminUser> getUserById(Long userId) {
        return adminUserRepository.findById(userId);
    }
}
