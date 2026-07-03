CREATE TABLE emergency_call_event (

    id UUID PRIMARY KEY,

    team_id UUID NOT NULL,

    caller_lat NUMERIC(10,7) NOT NULL,

    caller_lon NUMERIC(10,7) NOT NULL,

    caller_phone_number VARCHAR(20) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    converted_to_sos_id UUID,

    CONSTRAINT fk_call_event_team
        FOREIGN KEY (team_id)
        REFERENCES rescue_teams(id),

    CONSTRAINT fk_call_event_sos
        FOREIGN KEY (converted_to_sos_id)
        REFERENCES sos_requests(id),

    CONSTRAINT chk_call_event_status
        CHECK (status IN (
            'PENDING_MATCH',
            'MATCHED',
            'STALE'
        ))
);

CREATE INDEX idx_call_event_phone
ON emergency_call_event(caller_phone_number);

CREATE INDEX idx_call_event_team_status
ON emergency_call_event(team_id, status);