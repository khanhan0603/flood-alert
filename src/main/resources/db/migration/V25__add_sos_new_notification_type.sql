ALTER TABLE notifications
DROP CONSTRAINT chk_notification_type;

ALTER TABLE notifications
ADD CONSTRAINT chk_notification_type
CHECK (
    type IN (
        'SOS_NEW',
        'SOS_ASSIGNED',
        'ASSIGNMENT_FAILED',
        'SUPPORT_REQUEST_CREATED',
        'SUPPORT_REQUEST_CLAIMED',
        'SUPPORT_REQUEST_APPROVED',
        'SUPPORT_REQUEST_REJECTED',
        'SUPPORT_ASSIGNMENT_ASSIGNED',
        'SUPPORT_ASSIGNMENT_REJECTED',
        'CALL_WORKFLOW_FAILED'
    )
);