package com.petedillo.api.config;

import com.petedillo.api.security.AdminUserDetailsService;
import com.petedillo.api.security.JwtAuthenticationFilter;
import com.petedillo.api.security.JwtTokenProvider;
import com.petedillo.api.repository.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AdminUserRepository adminUserRepository;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService, 
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider tokenProvider,
                         AdminUserRepository adminUserRepository) {
        this.adminUserDetailsService = adminUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.adminUserRepository = adminUserRepository;
    }

    /**
     * Configure AuthenticationManager with DaoAuthenticationProvider
     * This is critical for form login to work - it tells Spring Security how to authenticate users
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Security filter chain for admin UI (/manage/**)
     * Uses form login with custom login page
     * OAuth2 login placeholder for future Gmail integration
     */
    @Bean
    @Order(4)
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
     * Security filter chain for protected API (/api/v1/admin/**)
     * Requires JWT authentication
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminApiSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider, adminUserRepository);

        http
            .securityMatcher("/api/v1/admin/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Security filter chain for auth API (/api/v1/auth/**)
     * Login is open, other endpoints require JWT authentication
     */
    @Bean
    @Order(2)
    public SecurityFilterChain authApiSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider, adminUserRepository);

        http
            .securityMatcher("/api/v1/auth/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Security filter chain for public API (/api/v1/**)
     * Public endpoints - no authentication required
     */
    @Bean
    @Order(3)
    public SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
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
    @Order(5)
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
    @Order(6)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
