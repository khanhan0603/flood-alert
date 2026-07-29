alter table iot_devices
ADD COLUMN device_height NUMERIC(5,2);

UPDATE iot_devices
SET device_height = 14.00
WHERE device_height IS NULL;

ALTER TABLE iot_devices
ALTER COLUMN device_height SET NOT NULL;