-- support_requests
ALTER TABLE support_requests
DROP CONSTRAINT IF EXISTS fkssxha98a7oohums9evm51d4sr;

ALTER TABLE support_requests
ADD CONSTRAINT fk_support_requests_sos
FOREIGN KEY (sos_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- sos_assignments
ALTER TABLE sos_assignments
DROP CONSTRAINT IF EXISTS fkj45q73q5t2dr8hjjsrl1x7imj;

ALTER TABLE sos_assignments
ADD CONSTRAINT fk_sos_assignments_sos
FOREIGN KEY (sos_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- emergency_call_event
ALTER TABLE emergency_call_event
DROP CONSTRAINT IF EXISTS fk_call_event_sos;

ALTER TABLE emergency_call_event
ADD CONSTRAINT fk_call_event_sos
FOREIGN KEY (converted_to_sos_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- notifications
ALTER TABLE notifications
DROP CONSTRAINT IF EXISTS fk_notification_sos;

ALTER TABLE notifications
ADD CONSTRAINT fk_notification_sos
FOREIGN KEY (sos_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- call_tasks
ALTER TABLE call_tasks
DROP CONSTRAINT IF EXISTS fk_call_task_sos_request;

ALTER TABLE call_tasks
ADD CONSTRAINT fk_call_task_sos_request
FOREIGN KEY (sos_request_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- call_logs
ALTER TABLE call_logs
DROP CONSTRAINT IF EXISTS fk_call_log_sos_request;

ALTER TABLE call_logs
ADD CONSTRAINT fk_call_log_sos_request
FOREIGN KEY (sos_request_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;

-- alarms
ALTER TABLE alarms
DROP CONSTRAINT IF EXISTS fk_alarm_sos_request;

ALTER TABLE alarms
ADD CONSTRAINT fk_alarm_sos_request
FOREIGN KEY (sos_request_id)
REFERENCES sos_requests(id)
ON DELETE CASCADE;