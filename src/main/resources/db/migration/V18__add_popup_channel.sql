ALTER TABLE notifications
DROP CONSTRAINT chk_notification_channel;

ALTER TABLE notifications
ADD CONSTRAINT chk_notification_channel
CHECK (
    channel IN (
        'EMAIL',
        'WEB_PUSH',
        'SMS',
        'POPUP'
    )
);