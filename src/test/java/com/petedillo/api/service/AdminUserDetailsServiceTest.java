package com.petedillo.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminUserDetailsServiceTest {

    private AdminUserDetailsService service;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new AdminUserDetailsService(passwordEncoder);

        // Set admin credentials using reflection (simulating @Value injection)
        ReflectionTestUtils.setField(service, "adminUsername", "admin");
        ReflectionTestUtils.setField(service, "adminPassword", "admin123");
    }

    @Test
    void whenLoadingValidUsername_thenReturnsUserDetails() {
        // When
        UserDetails userDetails = service.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }

    @Test
    void whenLoadingInvalidUsername_thenThrowsException() {
        // When / Then
        assertThatThrownBy(() -> service.loadUserByUsername("wronguser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: wronguser");
    }

    @Test
    void whenLoadingUserMultipleTimes_thenPasswordEncodingIsCached() {
        // When - load user twice
        UserDetails firstLoad = service.loadUserByUsername("admin");
        UserDetails secondLoad = service.loadUserByUsername("admin");

        // Then - both should have the same encoded password (proving it's cached)
        assertThat(firstLoad.getPassword()).isEqualTo(secondLoad.getPassword());

        // And the password should be BCrypt encoded (starts with $2a$, $2b$, or $2y$)
        assertThat(firstLoad.getPassword()).matches("^\\$2[aby]\\$.+");
    }

    @Test
    void whenPasswordIsCached_thenBCryptValidationWorks() {
        // When
        UserDetails userDetails = service.loadUserByUsername("admin");

        // Then - the cached password should validate correctly against the plaintext
        assertThat(passwordEncoder.matches("admin123", userDetails.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("wrongpassword", userDetails.getPassword())).isFalse();
    }

    @Test
    void whenLoadingUserWithNullUsername_thenThrowsException() {
        // When / Then
        assertThatThrownBy(() -> service.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void whenLoadingUserWithEmptyUsername_thenThrowsException() {
        // When / Then
        assertThatThrownBy(() -> service.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
