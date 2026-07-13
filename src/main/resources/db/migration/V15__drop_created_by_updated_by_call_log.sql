ALTER TABLE call_logs
DROP CONSTRAINT IF EXISTS fk_call_log_created_by;

ALTER TABLE call_logs
DROP CONSTRAINT IF EXISTS fk_call_log_updated_by;

ALTER TABLE call_logs
DROP COLUMN IF EXISTS created_by;

ALTER TABLE call_logs
DROP COLUMN IF EXISTS updated_by;