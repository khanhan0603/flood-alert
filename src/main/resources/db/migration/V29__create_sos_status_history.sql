CREATE TABLE sos_status_history (
    id UUID PRIMARY KEY,
    sos_id UUID NOT NULL REFERENCES sos_requests(id),
    status VARCHAR(50) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sos_status_history_sos ON sos_status_history (sos_id, created_at);