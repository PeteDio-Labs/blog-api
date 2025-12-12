-- Admin Users Table for Multi-Provider Authentication
CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- nullable for OAuth-only users
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL', -- LOCAL, GOOGLE, APPLE
    provider_user_id VARCHAR(255), -- OAuth provider's user ID
    display_name VARCHAR(255),
    profile_picture_url TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT unique_provider_user UNIQUE (auth_provider, provider_user_id)
);

-- Indexes for performance
CREATE INDEX idx_admin_users_email ON admin_users(email);
CREATE INDEX idx_admin_users_username ON admin_users(username);
CREATE INDEX idx_admin_users_provider ON admin_users(auth_provider, provider_user_id);

-- Comments for documentation
COMMENT ON TABLE admin_users IS 'Admin user accounts supporting multiple authentication providers';
COMMENT ON COLUMN admin_users.password_hash IS 'BCrypt hash - nullable for OAuth-only accounts';
COMMENT ON COLUMN admin_users.auth_provider IS 'Authentication provider: LOCAL, GOOGLE, or APPLE';
COMMENT ON COLUMN admin_users.provider_user_id IS 'Unique user ID from OAuth provider';
