package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

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
public class IoTDeviceCreationRequest {
    String deviceCode;
    String tenThietBi;
    BigDecimal lat;
    BigDecimal lon;
}
