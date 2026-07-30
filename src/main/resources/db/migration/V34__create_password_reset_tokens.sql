CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,

    token UUID NOT NULL UNIQUE,

    user_id UUID NOT NULL,

    expired_at TIMESTAMP NOT NULL,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token
    ON password_reset_tokens(token);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens(user_id);