ALTER TABLE rescue_groups
DROP CONSTRAINT rescue_groups_status_check;

ALTER TABLE rescue_groups
ADD CONSTRAINT rescue_groups_status_check
CHECK (
    status IN (
        'AVAILABLE',
        'BUSY',
        'OFFLINE',
        'DISBANDED'
    )
);