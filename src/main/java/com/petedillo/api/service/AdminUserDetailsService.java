package com.petedillo.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;
    private String encodedPassword;

    public AdminUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!adminUsername.equals(username)) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Encode password once and cache it
        // BCrypt generates different hashes each time, so we need to encode once and reuse
        if (encodedPassword == null) {
            encodedPassword = passwordEncoder.encode(adminPassword);
        }

        return User.builder()
            .username(adminUsername)
            .password(encodedPassword)
            .roles("ADMIN")
            .build();
    }

    // Future: Add OAuth2 user mapping here
    // public UserDetails loadOAuth2User(OAuth2User oauth2User) {
    //     String email = oauth2User.getAttribute("email");
    //     // Validate email against allowed admin emails
    //     // Return UserDetails with ADMIN role
    // }
}
