CREATE TABLE
    prediction_job_history (
        id UUID PRIMARY KEY,
        started_at TIMESTAMP NOT NULL,
        finished_at TIMESTAMP,
        job_type VARCHAR(20) NOT NULL,
        total_areas INTEGER NOT NULL,
        processed_areas INTEGER NOT NULL,
        high_risk_areas INTEGER NOT NULL,
        errors INTEGER NOT NULL,
        recovery_attempts INTEGER NOT NULL,
        recovered_areas INTEGER NOT NULL,
        remaining_missing INTEGER NOT NULL,
        status VARCHAR(20) NOT NULL,
        message VARCHAR(1000),
        CONSTRAINT chk_prediction_job_type CHECK (job_type IN ('MORNING', 'EVENING')),
        CONSTRAINT chk_prediction_job_status CHECK (
            status IN ('SUCCESS', 'PARTIAL_SUCCESS', 'FAILED')
        ),
        CONSTRAINT chk_total_areas CHECK (total_areas >= 0),
        CONSTRAINT chk_processed_areas CHECK (processed_areas >= 0),
        CONSTRAINT chk_high_risk_areas CHECK (high_risk_areas >= 0),
        CONSTRAINT chk_errors CHECK (errors >= 0),
        CONSTRAINT chk_recovery_attempts CHECK (recovery_attempts >= 0),
        CONSTRAINT chk_recovered_areas CHECK (recovered_areas >= 0),
        CONSTRAINT chk_remaining_missing CHECK (remaining_missing >= 0)
    );

CREATE INDEX idx_prediction_job_history_started_at ON prediction_job_history (started_at DESC);