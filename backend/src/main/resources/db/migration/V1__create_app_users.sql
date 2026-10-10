CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ NULL,

    CONSTRAINT ck_app_users_role
        CHECK (role IN ('ADMIN', 'ANALYST'))
);

CREATE INDEX idx_app_users_role
    ON app_users(role);

CREATE INDEX idx_app_users_enabled
    ON app_users(enabled);
