ALTER TABLE password_reset_tokens
ALTER COLUMN token TYPE VARCHAR(6)
USING LEFT(token::text, 6);