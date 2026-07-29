package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
public class IoTDeviceUpdateRequest {
    String tenThietBi;
    BigDecimal lat;
    BigDecimal lon;
    Double nguongCanhBao;
    BigDecimal deviceHeight;
}
