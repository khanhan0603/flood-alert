package com.example.flood_alert.dbo.response;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NearestSensorHistoryResponse {
    UUID deviceId;

    String deviceName;

    Double distanceMeters;

    List<SensorWaterHistoryResponse> histories;
}
