package com.petedillo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for current user info response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String authProvider;
    private Boolean isEnabled;
}
