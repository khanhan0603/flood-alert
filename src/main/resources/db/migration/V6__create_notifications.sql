CREATE TABLE notifications (
    id UUID PRIMARY KEY,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,

    type VARCHAR(50) NOT NULL,

    channel VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL,

    user_id UUID NOT NULL,

    sos_id UUID,

    assignment_id UUID,

    support_request_id UUID,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_notification_sos
        FOREIGN KEY (sos_id)
        REFERENCES sos_requests(id),

    CONSTRAINT fk_notification_assignment
        FOREIGN KEY (assignment_id)
        REFERENCES sos_assignments(id),

    CONSTRAINT fk_notification_support_request
        FOREIGN KEY (support_request_id)
        REFERENCES support_requests(id),

    CONSTRAINT chk_notification_type
        CHECK (
            type IN (
                'SOS_ASSIGNED',
                'SOS_OVERDUE',
                'ASSIGNMENT_FAILED',
                'SUPPORT_REQUEST_CREATED',
                'SUPPORT_REQUEST_APPROVED',
                'SUPPORT_REQUEST_REJECTED',
                'SUPPORT_ASSIGNMENT_ASSIGNED',
                'SUPPORT_ASSIGNMENT_REJECTED',
                'SYSTEM'
            )
        ),

    CONSTRAINT chk_notification_channel
        CHECK (
            channel IN (
                'EMAIL',
                'WEB_PUSH',
                'SMS'
            )
        ),

    CONSTRAINT chk_notification_status
        CHECK (
            status IN (
                'PENDING',
                'SENT',
                'FAILED'
            )
        )
);

CREATE INDEX idx_notification_user
ON notifications(user_id);

CREATE INDEX idx_notification_status
ON notifications(status);

CREATE INDEX idx_notification_channel
ON notifications(channel);

CREATE INDEX idx_notification_created_at
ON notifications(created_at DESC);

CREATE INDEX idx_notification_user_status
ON notifications(user_id, status);