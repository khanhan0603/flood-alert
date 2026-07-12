ALTER TABLE sos_requests
ADD COLUMN dispatcher_user_id UUID;

ALTER TABLE sos_requests
ADD COLUMN dispatcher_type VARCHAR(30);

ALTER TABLE sos_requests
ADD CONSTRAINT fk_sos_dispatcher_user
FOREIGN KEY (dispatcher_user_id)
REFERENCES users(id);

ALTER TABLE sos_requests
ADD CONSTRAINT chk_sos_dispatcher_type
CHECK (
    dispatcher_type IS NULL OR
    dispatcher_type IN (
        'TEAM_LEADER',
        'DEPUTY_LEADER',
        'PROVINCE_OPERATOR'
    )
);