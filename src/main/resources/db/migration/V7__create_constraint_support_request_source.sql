ALTER TABLE support_requests
ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'TEAM';

ALTER TABLE support_requests
ADD CONSTRAINT chk_support_request_source
CHECK (
    source IN (
        'GROUP',
        'TEAM'
    )
);