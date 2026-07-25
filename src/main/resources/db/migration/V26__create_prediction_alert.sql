CREATE TABLE prediction_alerts
(
    id UUID PRIMARY KEY,

    prediction_job_history_id UUID NOT NULL,

    area_id UUID NOT NULL,

    prediction_date DATE NOT NULL,

    risk_level VARCHAR(20) NOT NULL,

    title VARCHAR(255) NOT NULL,

    message TEXT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_prediction_alert_history
        FOREIGN KEY (prediction_job_history_id)
        REFERENCES prediction_job_history(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prediction_alert_area
        FOREIGN KEY (area_id)
        REFERENCES areas(id)
);

CREATE INDEX idx_prediction_alert_history
ON prediction_alerts(prediction_job_history_id);

CREATE INDEX idx_prediction_alert_area
ON prediction_alerts(area_id);

CREATE INDEX idx_prediction_alert_status
ON prediction_alerts(status);

CREATE INDEX idx_prediction_alert_created_at
ON prediction_alerts(created_at);

CREATE UNIQUE INDEX uq_prediction_alert
ON prediction_alerts(
    prediction_job_history_id,
    area_id,
    prediction_date
);