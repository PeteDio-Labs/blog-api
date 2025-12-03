package com.petedillo.api.config;

import com.petedillo.api.service.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService) {
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain for admin UI (/manage/**)
     * Uses form login with custom login page
     * OAuth2 login placeholder for future Gmail integration
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/manage/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/manage/login").permitAll()
                .requestMatchers("/manage/**").authenticated()
            )
            .formLogin(form -> form
                .loginPage("/manage/login")
                .loginProcessingUrl("/manage/login")
                .defaultSuccessUrl("/manage/posts", true)
                .failureUrl("/manage/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/manage/logout")
                .logoutSuccessUrl("/manage/login?logout=true")
                .permitAll()
            )
            // Future OAuth2 login (not implemented yet)
            // .oauth2Login(oauth2 -> oauth2
            //     .loginPage("/manage/login")
            //     .defaultSuccessUrl("/manage/posts", true)
            // )
            .userDetailsService(adminUserDetailsService)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/manage/api/**") // AJAX endpoints
            );

        return http.build();
    }

    /**
     * Security filter chain for public API (/api/v1/**)
     * Completely open, no authentication required
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Security filter chain for health and actuator endpoints
     * Open access for monitoring
     */
    @Bean
    @Order(3)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**", "/health")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Default security filter chain
     * Catches all other requests
     */
    @Bean
    @Order(4)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
