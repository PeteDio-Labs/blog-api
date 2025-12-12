-- Refresh Tokens Table for JWT Token Rotation
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    admin_user_id BIGINT NOT NULL,
    token_family_id UUID NOT NULL, -- Track token rotation family
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    CONSTRAINT fk_refresh_tokens_admin_user FOREIGN KEY (admin_user_id)
        REFERENCES admin_users(id) ON DELETE CASCADE
);

-- Indexes for fast lookups
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(admin_user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(token_family_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Comments
COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens with rotation tracking';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of refresh token';
COMMENT ON COLUMN refresh_tokens.token_family_id IS 'UUID linking rotated tokens for reuse detection';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'True if token or family has been revoked';
