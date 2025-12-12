package com.petedillo.api.security;

import com.petedillo.api.model.AdminUser;
import com.petedillo.api.repository.AdminUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter for validating Bearer tokens in Authorization header or cookies.
 * Sets authenticated user in SecurityContext if token is valid.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final AdminUserRepository adminUserRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, AdminUserRepository adminUserRepository) {
        this.tokenProvider = tokenProvider;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (token != null && !token.isEmpty()) {
                Long userId = tokenProvider.extractUserId(token);

                Optional<AdminUser> user = adminUserRepository.findById(userId);

                if (user.isPresent() && user.get().getIsEnabled()) {
                    AdminUserDetails adminUserDetails = new AdminUserDetails(user.get());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(adminUserDetails, null, adminUserDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Token validation failed - continue without authentication
            logger.debug("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header or cookies.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not found
     */
    private String extractToken(HttpServletRequest request) {
        // Try Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Try accessToken cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
