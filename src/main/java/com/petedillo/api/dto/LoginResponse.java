package com.petedillo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing user info and tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(Long userId, String username, String email, String displayName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }
}

