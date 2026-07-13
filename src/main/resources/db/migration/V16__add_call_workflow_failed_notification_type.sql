-- Xóa constraint cũ
ALTER TABLE notifications
DROP CONSTRAINT chk_notification_type;

-- Tạo lại constraint mới
ALTER TABLE notifications
ADD CONSTRAINT chk_notification_type
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

        'CALL_WORKFLOW_FAILED'
    )
);