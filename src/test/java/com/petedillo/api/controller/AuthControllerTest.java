package com.petedillo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petedillo.api.dto.LoginRequest;
import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import com.petedillo.api.repository.AdminUserRepository;
import com.petedillo.api.security.JwtTokenProvider;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private AdminUser testUser;
    private String testPassword = "password123";

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.adminUserBuilder()
                .username("testuser")
                .email("test@example.com")
                .authProvider(AuthProvider.LOCAL)
                .passwordHash(passwordEncoder.encode(testPassword))
                .isEnabled(true)
                .build();
        adminUserRepository.save(testUser);
    }

    @Test
    @DisplayName("should login with valid credentials and return tokens")
    void testLoginSuccess() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("should return 401 with invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 401 with non-existent user")
    void testLoginNonExistentUser() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 401 when user is disabled")
    void testLoginDisabledUser() throws Exception {
        // Arrange
        testUser.setIsEnabled(false);
        adminUserRepository.save(testUser);
        LoginRequest request = new LoginRequest("testuser", testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should logout successfully with valid token")
    void testLogout() throws Exception {
        // Generate valid token
        String token = tokenProvider.generateAccessToken(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return 401 or 403 when getting current user without token")
    void testGetCurrentUserNoToken() throws Exception {
        // Act & Assert - Either 401 (Unauthorized) or 403 (Forbidden) is acceptable
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Expected 401 or 403 but was " + status);
                    }
                });
    }

    @Test
    @DisplayName("should return current user info with valid token")
    void testGetCurrentUserWithToken() throws Exception {
        // Generate valid token
        String token = tokenProvider.generateAccessToken(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
