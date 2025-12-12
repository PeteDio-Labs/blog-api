package com.petedillo.api.model;

/**
 * Authentication provider enum.
 * Supports LOCAL (password-based) and OAuth providers (Google, Apple, etc).
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE,
    APPLE
}
