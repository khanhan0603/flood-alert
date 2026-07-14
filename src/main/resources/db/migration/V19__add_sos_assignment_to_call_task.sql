ALTER TABLE call_tasks
ADD COLUMN assignment_id UUID;

ALTER TABLE call_tasks
ADD CONSTRAINT fk_call_task_assignment
FOREIGN KEY (assignment_id)
REFERENCES sos_assignments(id);

CREATE INDEX idx_call_task_assignment
ON call_tasks(assignment_id);