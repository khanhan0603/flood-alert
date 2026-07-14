ALTER TABLE support_requests
ADD COLUMN dispatcher_user_id UUID;

ALTER TABLE support_requests
ADD CONSTRAINT fk_support_request_dispatcher
FOREIGN KEY (dispatcher_user_id)
REFERENCES users(id);

CREATE INDEX idx_support_request_dispatcher
ON support_requests(dispatcher_user_id);