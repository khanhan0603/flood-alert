CREATE TABLE call_tasks (
    id UUID PRIMARY KEY,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    target_user_id UUID NOT NULL,

    target_type VARCHAR(40) NOT NULL,

    retry_count INTEGER NOT NULL DEFAULT 0,

    timeout_seconds INTEGER NOT NULL,

    status VARCHAR(40) NOT NULL,

    sos_request_id UUID,

    support_request_id UUID,

    CONSTRAINT fk_call_task_target_user
        FOREIGN KEY (target_user_id)
        REFERENCES users(id),

    CONSTRAINT fk_call_task_sos_request
        FOREIGN KEY (sos_request_id)
        REFERENCES sos_requests(id),

    CONSTRAINT fk_call_task_support_request
        FOREIGN KEY (support_request_id)
        REFERENCES support_requests(id),

    CONSTRAINT chk_call_task_target_type
        CHECK (
            target_type IN (
                'TEAM_LEADER',
                'DEPUTY_LEADER',
                'PROVINCE_OPERATOR',
                'GROUP_LEADER',
                'SUPPORT_TEAM_LEADER',
                'SUPPORT_TEAM_DEPUTY'
            )
        ),

    CONSTRAINT chk_call_task_status
        CHECK (
            status IN (
                'CALLING_TEAM_LEADER',
                'CALLING_DEPUTY',
                'CALLING_PROVINCE',
                'CALLING_GROUP_LEADER',
                'CALLING_SUPPORT_TEAM_LEADER',
                'CALLING_SUPPORT_TEAM_DEPUTY',
                'SUCCESS',
                'FAILED'
            )
        ),

    CONSTRAINT chk_call_task_reference
        CHECK (
            (sos_request_id IS NOT NULL AND support_request_id IS NULL)
            OR
            (sos_request_id IS NULL AND support_request_id IS NOT NULL)
        )
);

CREATE INDEX idx_call_task_sos_request
ON call_tasks (sos_request_id);

CREATE INDEX idx_call_task_support_request
ON call_tasks (support_request_id);

CREATE INDEX idx_call_task_status
ON call_tasks (status);

CREATE INDEX idx_call_task_target_user
ON call_tasks (target_user_id);