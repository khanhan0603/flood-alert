-- =====================================================
-- Add Hotline fields to SOS Request
-- =====================================================

-- =====================================================
-- User tạo SOS (Operator hoặc người dân)
-- =====================================================

ALTER TABLE sos_requests
ADD COLUMN created_by_user_id UUID;

ALTER TABLE sos_requests
ADD CONSTRAINT fk_sos_created_by_user
FOREIGN KEY (created_by_user_id)
REFERENCES users(id);

-- =====================================================
-- Nguồn tạo SOS
-- =====================================================

ALTER TABLE sos_requests
ADD COLUMN sos_source VARCHAR(30);

UPDATE sos_requests
SET sos_source = 'DIRECT'
WHERE sos_source IS NULL;

ALTER TABLE sos_requests
ALTER COLUMN sos_source SET NOT NULL;

ALTER TABLE sos_requests
ADD CONSTRAINT chk_sos_source
CHECK (sos_source IN (
    'DIRECT',
    'HOTLINE_OPERATOR'
));

-- =====================================================
-- Emergency Call Event dùng để tạo SOS
-- =====================================================

ALTER TABLE sos_requests
ADD COLUMN linked_call_event_id UUID;

ALTER TABLE sos_requests
ADD CONSTRAINT fk_sos_call_event
FOREIGN KEY (linked_call_event_id)
REFERENCES emergency_call_event(id);

-- =====================================================
-- Nguồn lấy vị trí
-- =====================================================

ALTER TABLE sos_requests
ADD COLUMN location_source VARCHAR(30);

ALTER TABLE sos_requests
ADD CONSTRAINT chk_location_source
CHECK (location_source IN (
    'GPS_FROM_CALL_EVENT',
    'MANUAL_ADDRESS'
));

-- =====================================================
-- Tracking Code
-- =====================================================

ALTER TABLE sos_requests
ADD COLUMN tracking_code VARCHAR(6);

CREATE UNIQUE INDEX uq_sos_tracking_code
ON sos_requests(tracking_code);

COMMENT ON COLUMN sos_requests.created_by_user_id IS
'User tạo SOS (Operator hoặc người dân).';

COMMENT ON COLUMN sos_requests.sos_source IS
'Nguồn tạo SOS: DIRECT hoặc HOTLINE_OPERATOR.';

COMMENT ON COLUMN sos_requests.linked_call_event_id IS
'EmergencyCallEvent được dùng để tạo SOS.';

COMMENT ON COLUMN sos_requests.location_source IS
'Nguồn lấy vị trí: GPS_FROM_CALL_EVENT hoặc MANUAL_ADDRESS.';

COMMENT ON COLUMN sos_requests.tracking_code IS
'Mã tra cứu trạng thái SOS.';