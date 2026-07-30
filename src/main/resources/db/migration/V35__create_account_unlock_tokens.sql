CREATE TABLE account_unlock_tokens (
    id UUID PRIMARY KEY,

    otp VARCHAR(6) NOT NULL UNIQUE,

    user_id UUID NOT NULL,

    expired_at TIMESTAMP NOT NULL,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_account_unlock_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_account_unlock_tokens_user_id
ON account_unlock_tokens(user_id);

CREATE INDEX idx_account_unlock_tokens_expired_at
ON account_unlock_tokens(expired_at);