package com.petedillo.api.controller;

import com.petedillo.api.dto.CurrentUserResponse;
import com.petedillo.api.dto.LoginRequest;
import com.petedillo.api.dto.LoginResponse;
import com.petedillo.api.dto.RefreshTokenRequest;
import com.petedillo.api.model.AdminUser;
import com.petedillo.api.security.AdminUserDetails;
import com.petedillo.api.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for authentication endpoints.
 * Handles login, logout, token refresh, and current user info.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private static final int ACCESS_TOKEN_MAX_AGE = 15 * 60; // 15 minutes
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint with username and password.
     * Returns user info and sets httpOnly cookies with tokens.
     *
     * @param request the login request
     * @param response the HTTP response to set cookies
     * @return user info with access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response, HttpServletRequest httpRequest) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }

        Optional<AdminUser> user = authService.authenticateUser(request.getUsername(), request.getPassword());

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        AdminUser adminUser = user.get();
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        // Generate both tokens and store refresh token in DB
        String[] tokens = authService.generateTokens(adminUser, userAgent, ipAddress);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        // Set httpOnly cookies
        setCookie(response, "accessToken", accessToken, ACCESS_TOKEN_MAX_AGE);
        setCookie(response, "refreshToken", refreshToken, REFRESH_TOKEN_MAX_AGE);

        LoginResponse loginResponse = new LoginResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getDisplayName(),
                adminUser.getAuthProvider().toString(),
                accessToken,
                refreshToken
        );

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Logout endpoint to clear authentication tokens.
     *
     * @param response the HTTP response
     * @param authentication the current authentication
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails) {
            AdminUserDetails userDetails = (AdminUserDetails) authentication.getPrincipal();
            authService.revokeUserTokens(userDetails.getAdminUserId());
        }

        // Clear cookies
        clearCookie(response, "accessToken");
        clearCookie(response, "refreshToken");

        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh access token using refresh token.
     * Supports both cookie-based and request body refresh tokens.
     *
     * @param request optional refresh token in request body
     * @param response the HTTP response
     * @param httpRequest the HTTP request containing cookies
     * @return new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response,
            HttpServletRequest httpRequest) {
        
        String refreshToken = null;

        // First try to get from request body
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            refreshToken = request.getRefreshToken();
        } else {
            // Fall back to cookies
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
        }

        Optional<String> newAccessToken = authService.refreshAccessToken(refreshToken);

        if (newAccessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        setCookie(response, "accessToken", newAccessToken.get(), ACCESS_TOKEN_MAX_AGE);
        setCookie(response, "refreshToken", refreshToken, REFRESH_TOKEN_MAX_AGE);

        // Return new token in response body for API clients
        LoginResponse refreshResponse = new LoginResponse();
        refreshResponse.setAccessToken(newAccessToken.get());
        return ResponseEntity.ok(refreshResponse);
    }

    /**
     * Get current authenticated user information.
     *
     * @param authentication the current authentication
     * @return current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        AdminUserDetails userDetails = (AdminUserDetails) authentication.getPrincipal();
        AdminUser adminUser = userDetails.getAdminUser();

        CurrentUserResponse response = new CurrentUserResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getDisplayName(),
                adminUser.getAuthProvider().toString(),
                adminUser.getIsEnabled()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Set an httpOnly secure cookie.
     */
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Clear a cookie by setting max age to 0.
     */
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
