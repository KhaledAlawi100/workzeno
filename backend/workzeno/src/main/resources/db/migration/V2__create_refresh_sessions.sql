
CREATE TABLE refresh_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    family_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,
    replaced_by_id UUID,

    CONSTRAINT fk_refresh_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_refresh_sessions_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_sessions_replaced_by
        FOREIGN KEY (replaced_by_id)
        REFERENCES refresh_sessions (id),

    CONSTRAINT chk_refresh_sessions_expiry
        CHECK (expires_at > created_at),

    CONSTRAINT chk_refresh_sessions_token_hash
        CHECK (token_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX idx_refresh_sessions_user_id
    ON refresh_sessions (user_id);

CREATE INDEX idx_refresh_sessions_family_id
    ON refresh_sessions (family_id);

CREATE INDEX idx_refresh_sessions_expires_at
    ON refresh_sessions (expires_at);
