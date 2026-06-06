package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTDeviceCreationResponse {
    String id;
    String device_code;
    String area_id;
    String tenkhuvuc;
    String ten_thietbi;
    BigDecimal lat;
    BigDecimal lon;
    String trang_thai;
    String createdAt;
    String updatedAt;
}
