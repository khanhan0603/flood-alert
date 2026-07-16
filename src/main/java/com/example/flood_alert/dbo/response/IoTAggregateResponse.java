package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
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
    BigDecimal avgWater;;
    BigDecimal maxWater;
    BigDecimal minWater;
    BigDecimal currentWater;
    Integer totalDeviceCount;
    BigDecimal waterRiseRatePerMinute;
    Double dangerRatio;
    Integer dangerDurationMinutes;
    LocalDateTime recordedAt;
}
