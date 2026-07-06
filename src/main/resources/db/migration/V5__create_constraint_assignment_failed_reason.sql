ALTER TABLE sos_assignments
ADD COLUMN failed_reason VARCHAR(50);

ALTER TABLE sos_assignments
ADD COLUMN failed_note VARCHAR(500);

ALTER TABLE sos_assignments
ADD COLUMN failed_at TIMESTAMP;

ALTER TABLE sos_assignments
ADD CONSTRAINT chk_sos_assignments_failed_reason
CHECK (
    failed_reason IS NULL OR
    failed_reason IN (
        'BOAT_BROKEN',
        'VEHICLE_BROKEN',
        'CANNOT_ACCESS',
        'LOST_CONTACT',
        'OTHER'
    )
);