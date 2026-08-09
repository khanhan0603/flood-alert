ALTER TABLE iot_area_aggregates
ALTER COLUMN danger_duration_minutes TYPE NUMERIC(10,2)
USING danger_duration_minutes::NUMERIC(10,2);

ALTER TABLE area_risk_snapshots
ALTER COLUMN danger_duration_minutes TYPE NUMERIC(10,2)
USING danger_duration_minutes::NUMERIC(10,2);