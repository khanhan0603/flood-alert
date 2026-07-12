CREATE TABLE call_logs (
    id UUID PRIMARY KEY,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    caller_user_id UUID NOT NULL,

    receiver_user_id UUID NOT NULL,

    phone_number VARCHAR(20) NOT NULL,

    call_type VARCHAR(30) NOT NULL,

    call_result VARCHAR(30) NOT NULL,

    attempt INTEGER NOT NULL,

    started_at TIMESTAMP NOT NULL,

    ended_at TIMESTAMP,

    sos_request_id UUID,

    support_request_id UUID,

    CONSTRAINT fk_call_log_caller_user
        FOREIGN KEY (caller_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_call_log_receiver_user
        FOREIGN KEY (receiver_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_call_log_sos_request
        FOREIGN KEY (sos_request_id)
        REFERENCES sos_requests(id),

    CONSTRAINT fk_call_log_support_request
        FOREIGN KEY (support_request_id)
        REFERENCES support_requests(id),

    CONSTRAINT chk_call_log_type
        CHECK (
            call_type IN (
                'SOS',
                'SUPPORT_REQUEST'
            )
        ),

    CONSTRAINT chk_call_log_result
        CHECK (
            call_result IN (
                'ANSWERED',
                'NO_ANSWER',
                'REJECTED',
                'TIMEOUT',
                'FAILED'
            )
        ),

    CONSTRAINT chk_call_log_reference
        CHECK (
            (sos_request_id IS NOT NULL AND support_request_id IS NULL)
            OR
            (sos_request_id IS NULL AND support_request_id IS NOT NULL)
        )
);

CREATE INDEX idx_call_log_caller_user
ON call_logs (caller_user_id);

CREATE INDEX idx_call_log_receiver_user
ON call_logs (receiver_user_id);

CREATE INDEX idx_call_log_sos_request
ON call_logs (sos_request_id);

CREATE INDEX idx_call_log_support_request
ON call_logs (support_request_id);

CREATE INDEX idx_call_log_call_type
ON call_logs (call_type);

CREATE INDEX idx_call_log_call_result
ON call_logs (call_result);

CREATE INDEX idx_call_log_started_at
ON call_logs (started_at);