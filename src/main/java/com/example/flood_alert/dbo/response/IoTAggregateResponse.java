package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTAggregateResponse {
    UUID area_id;
    String tenkhuvuc;
    Double avgWater;;
    Double maxWater;
    Double minWater;
    Double currentWater;
    Integer totalDeviceCount;
    Double waterRiseRatePerMinute;
    Double dangerRatio;
    LocalDateTime recordedAt;
}
