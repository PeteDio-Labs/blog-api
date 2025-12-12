package com.petedillo.api.security;

import com.petedillo.api.model.AdminUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails implementation for AdminUser.
 * Adapts AdminUser JPA entity to Spring Security's UserDetails interface.
 */
public class AdminUserDetails implements UserDetails {

    private final AdminUser adminUser;

    public AdminUserDetails(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public String getUsername() {
        return adminUser.getUsername();
    }

    @Override
    public String getPassword() {
        return adminUser.getPasswordHash();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return adminUser.getIsEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Admin accounts don't expire
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials don't expire (JWT handles expiry)
    }

    @Override
    public boolean isEnabled() {
        return adminUser.getIsEnabled();
    }

    /**
     * Get the admin user ID.
     *
     * @return the admin user ID
     */
    public Long getAdminUserId() {
        return adminUser.getId();
    }

    /**
     * Get the admin user email.
     *
     * @return the email address
     */
    public String getEmail() {
        return adminUser.getEmail();
    }

    /**
     * Get the underlying AdminUser entity.
     *
     * @return the AdminUser entity
     */
    public AdminUser getAdminUser() {
        return adminUser;
    }
}
