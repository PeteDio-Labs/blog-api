package com.petedillo.api.security;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AdminUserDetails Tests")
class AdminUserDetailsTest {

    private AdminUser adminUser;
    private AdminUserDetails adminUserDetails;

    @BeforeEach
    void setUp() {
        adminUser = TestDataFactory.adminUserBuilder()
                .username("testadmin")
                .email("test@example.com")
                .isEnabled(true)
                .build();
        adminUser.setId(1L);
        
        adminUserDetails = new AdminUserDetails(adminUser);
    }

    @Test
    @DisplayName("should create AdminUserDetails from AdminUser entity")
    void testAdminUserDetailsCreation() {
        // Assert
        assertThat(adminUserDetails).isNotNull();
        assertThat(adminUserDetails.getUsername()).isEqualTo("testadmin");
    }

    @Test
    @DisplayName("should return username correctly")
    void testGetUsername() {
        // Act
        String username = adminUserDetails.getUsername();

        // Assert
        assertThat(username).isEqualTo("testadmin");
    }

    @Test
    @DisplayName("should return password hash correctly")
    void testGetPassword() {
        // Arrange
        String expectedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhKu";
        AdminUser user = TestDataFactory.adminUserBuilder()
                .username("hashtest")
                .passwordHash(expectedHash)
                .build();
        AdminUserDetails details = new AdminUserDetails(user);

        // Act
        String password = details.getPassword();

        // Assert
        assertThat(password).isEqualTo(expectedHash);
    }

    @Test
    @DisplayName("should return true when account is enabled")
    void testIsAccountNonLocked() {
        // Assert
        assertThat(adminUserDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("should return false when account is disabled")
    void testIsAccountLockedWhenDisabled() {
        // Arrange
        AdminUser disabledUser = TestDataFactory.adminUserBuilder()
                .username("disabled")
                .isEnabled(false)
                .build();
        AdminUserDetails details = new AdminUserDetails(disabledUser);

        // Assert
        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("should return true for non-expired credentials")
    void testIsCredentialsNonExpired() {
        // Assert
        assertThat(adminUserDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("should return true when account is enabled")
    void testIsEnabled() {
        // Assert
        assertThat(adminUserDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should return false when account is disabled")
    void testIsEnabledReturnsFalseForDisabledAccount() {
        // Arrange
        AdminUser disabledUser = TestDataFactory.adminUserBuilder()
                .username("disabled")
                .isEnabled(false)
                .build();
        AdminUserDetails details = new AdminUserDetails(disabledUser);

        // Assert
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("should return admin authority")
    void testGetAuthorities() {
        // Act
        Collection<? extends GrantedAuthority> authorities = adminUserDetails.getAuthorities();

        // Assert
        assertThat(authorities).isNotEmpty();
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("should return admin user id")
    void testGetAdminUserId() {
        // Act
        Long userId = adminUserDetails.getAdminUserId();

        // Assert
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("should return admin user email")
    void testGetEmail() {
        // Act
        String email = adminUserDetails.getEmail();

        // Assert
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("should handle null password hash for OAuth users")
    void testNullPasswordHashForOAuthUser() {
        // Arrange
        AdminUser oauthUser = TestDataFactory.adminUserBuilder()
                .username("google_user")
                .email("google@example.com")
                .authProvider(AuthProvider.GOOGLE)
                .passwordHash(null)
                .build();
        AdminUserDetails details = new AdminUserDetails(oauthUser);

        // Act
        String password = details.getPassword();

        // Assert
        assertThat(password).isNull();
    }
}
