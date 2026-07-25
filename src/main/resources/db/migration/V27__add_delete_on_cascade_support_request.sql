-- support_request_items
ALTER TABLE support_request_items
DROP CONSTRAINT fkferlpliwfi2swur5qk65kk3li;

ALTER TABLE support_request_items
ADD CONSTRAINT fkferlpliwfi2swur5qk65kk3li
FOREIGN KEY (support_request_id)
REFERENCES support_requests(id)
ON DELETE CASCADE;

-- notifications
ALTER TABLE notifications
DROP CONSTRAINT fk_notification_support_request;

ALTER TABLE notifications
ADD CONSTRAINT fk_notification_support_request
FOREIGN KEY (support_request_id)
REFERENCES support_requests(id)
ON DELETE CASCADE;

-- call_tasks
ALTER TABLE call_tasks
DROP CONSTRAINT fk_call_task_support_request;

ALTER TABLE call_tasks
ADD CONSTRAINT fk_call_task_support_request
FOREIGN KEY (support_request_id)
REFERENCES support_requests(id)
ON DELETE CASCADE;

-- call_logs
ALTER TABLE call_logs
DROP CONSTRAINT fk_call_log_support_request;

ALTER TABLE call_logs
ADD CONSTRAINT fk_call_log_support_request
FOREIGN KEY (support_request_id)
REFERENCES support_requests(id)
ON DELETE CASCADE;