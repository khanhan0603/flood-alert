ALTER TABLE flood_alerts
DROP CONSTRAINT IF EXISTS flood_alerts_channel_check;

ALTER TABLE flood_alerts
ADD CONSTRAINT flood_alerts_channel_check
CHECK (
    channel IN (
        'EMAIL',
        'SMS',
        'WEB_PUSH',
        'POPUP'
    )
);