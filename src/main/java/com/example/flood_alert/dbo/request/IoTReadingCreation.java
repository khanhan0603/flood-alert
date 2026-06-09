package com.example.flood_alert.dbo.request;
import java.time.LocalDateTime;

import com.example.flood_alert.enums.WaterStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IoTReadingCreation {
    String deviceCode;
    Double waterLevel;
    WaterStatus status;
    boolean isValid;
    LocalDateTime recordedAt;
}
