ALTER TABLE iot_sensor_readings
ALTER COLUMN water_level TYPE NUMERIC(10,2)
USING ROUND(water_level::numeric,2);

ALTER TABLE iot_area_aggregates
ALTER COLUMN avg_water TYPE NUMERIC(10,2)
USING ROUND(avg_water::numeric,2);

ALTER TABLE iot_area_aggregates
ALTER COLUMN min_water TYPE NUMERIC(10,2)
USING ROUND(min_water::numeric,2);

ALTER TABLE iot_area_aggregates
ALTER COLUMN max_water TYPE NUMERIC(10,2)
USING ROUND(max_water::numeric,2);

ALTER TABLE iot_area_aggregates
ALTER COLUMN current_water TYPE NUMERIC(10,2)
USING ROUND(current_water::numeric,2);

ALTER TABLE iot_area_aggregates
ALTER COLUMN water_rise_rate_per_minute TYPE NUMERIC(10,2)
USING ROUND(water_rise_rate_per_minute::numeric,2);

ALTER TABLE sos_requests
ALTER COLUMN snapshot_water_rise TYPE NUMERIC(10,2)
USING ROUND(snapshot_water_rise::numeric, 2);