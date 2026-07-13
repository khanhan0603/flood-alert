CREATE TABLE alarms (

    id UUID PRIMARY KEY,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    title VARCHAR(200) NOT NULL,

    message TEXT NOT NULL,

    call_task_id UUID NOT NULL,

    sos_request_id UUID NOT NULL,

    CONSTRAINT fk_alarm_call_task
        FOREIGN KEY (call_task_id)
        REFERENCES call_tasks(id),

    CONSTRAINT fk_alarm_sos_request
        FOREIGN KEY (sos_request_id)
        REFERENCES sos_requests(id)
);

CREATE INDEX idx_alarm_call_task
ON alarms(call_task_id);

CREATE INDEX idx_alarm_sos
ON alarms(sos_request_id);