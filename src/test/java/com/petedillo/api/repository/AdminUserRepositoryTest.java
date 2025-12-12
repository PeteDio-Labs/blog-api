package com.petedillo.api.repository;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AdminUserRepository Tests")
class AdminUserRepositoryTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    private AdminUser testAdminUser;

    @BeforeEach
    void setUp() {
        testAdminUser = TestDataFactory.adminUserBuilder()
                .username("johndoe")
                .email("john@example.com")
                .build();
    }

    @Test
    @DisplayName("should save and retrieve admin user by email")
    void testSaveAndFindByEmail() {
        // Arrange
        AdminUser user = TestDataFactory.adminUserBuilder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hash")
                .build();

        // Act
        AdminUser saved = adminUserRepository.save(user);
        Optional<AdminUser> found = adminUserRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("should find admin user by username")
    void testFindByUsername() {
        // Arrange
        adminUserRepository.save(testAdminUser);

        // Act
        Optional<AdminUser> found = adminUserRepository.findByUsername("johndoe");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("should find admin user by auth provider and provider user id")
    void testFindByAuthProviderAndProviderUserId() {
        // Arrange
        AdminUser oauthUser = TestDataFactory.adminUserBuilder()
                .username("google_user")
                .email("google@example.com")
                .authProvider(AuthProvider.GOOGLE)
                .providerUserId("google_12345")
                .passwordHash(null) // OAuth users may not have password
                .build();
        adminUserRepository.save(oauthUser);

        // Act
        Optional<AdminUser> found = adminUserRepository.findByAuthProviderAndProviderUserId(
                AuthProvider.GOOGLE, "google_12345"
        );

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("google_user");
    }

    @Test
    @DisplayName("should return empty optional when user not found")
    void testFindNonExistentUser() {
        // Act
        Optional<AdminUser> found = adminUserRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should enforce unique email constraint")
    void testUniqueEmailConstraint() {
        // Arrange
        AdminUser user1 = TestDataFactory.adminUserBuilder()
                .username("user1")
                .email("duplicate@example.com")
                .build();
        AdminUser user2 = TestDataFactory.adminUserBuilder()
                .username("user2")
                .email("duplicate@example.com")
                .build();

        // Act & Assert
        adminUserRepository.save(user1);
        assertThatThrownBy(() -> {
            adminUserRepository.save(user2);
            adminUserRepository.flush();
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException wrapped
    }

    @Test
    @DisplayName("should enforce unique username constraint")
    void testUniqueUsernameConstraint() {
        // Arrange
        AdminUser user1 = TestDataFactory.adminUserBuilder()
                .username("duplicate_user")
                .email("email1@example.com")
                .build();
        AdminUser user2 = TestDataFactory.adminUserBuilder()
                .username("duplicate_user")
                .email("email2@example.com")
                .build();

        // Act & Assert
        adminUserRepository.save(user1);
        assertThatThrownBy(() -> {
            adminUserRepository.save(user2);
            adminUserRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("should update admin user")
    void testUpdateAdminUser() {
        // Arrange
        AdminUser saved = adminUserRepository.save(testAdminUser);
        Long userId = saved.getId();

        // Act
        saved.setDisplayName("John Doe Updated");
        saved.setIsEnabled(false);
        adminUserRepository.save(saved);

        // Assert
        AdminUser updated = adminUserRepository.findById(userId).orElseThrow();
        assertThat(updated.getDisplayName()).isEqualTo("John Doe Updated");
        assertThat(updated.getIsEnabled()).isFalse();
    }

    @Test
    @DisplayName("should delete admin user")
    void testDeleteAdminUser() {
        // Arrange
        AdminUser saved = adminUserRepository.save(testAdminUser);
        Long userId = saved.getId();

        // Act
        adminUserRepository.deleteById(userId);

        // Assert
        Optional<AdminUser> deleted = adminUserRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }
}
