package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    Integer dangerDurationMinutes;
    LocalDateTime recordedAt;
}
