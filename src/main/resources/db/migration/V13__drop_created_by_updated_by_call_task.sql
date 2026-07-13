ALTER TABLE call_tasks
DROP CONSTRAINT IF EXISTS fk_call_task_created_by;

ALTER TABLE call_tasks
DROP CONSTRAINT IF EXISTS fk_call_task_updated_by;

ALTER TABLE call_tasks
DROP COLUMN IF EXISTS created_by;

ALTER TABLE call_tasks
DROP COLUMN IF EXISTS updated_by;