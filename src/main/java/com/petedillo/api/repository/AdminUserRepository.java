package com.petedillo.api.repository;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AdminUser entities.
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    /**
     * Find an admin user by email address.
     *
     * @param email the email address
     * @return an Optional containing the user if found
     */
    Optional<AdminUser> findByEmail(String email);

    /**
     * Find an admin user by username.
     *
     * @param username the username
     * @return an Optional containing the user if found
     */
    Optional<AdminUser> findByUsername(String username);

    /**
     * Find an admin user by authentication provider and provider user ID.
     * Used for OAuth login flows.
     *
     * @param authProvider the authentication provider
     * @param providerUserId the user ID from the OAuth provider
     * @return an Optional containing the user if found
     */
    Optional<AdminUser> findByAuthProviderAndProviderUserId(AuthProvider authProvider, String providerUserId);
}
