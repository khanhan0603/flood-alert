package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorWaterHistoryResponse {
    Double waterLevel;

    LocalDateTime recordedAt;
}
