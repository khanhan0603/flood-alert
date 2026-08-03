package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.DeviceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IoTDeviceDetailResponse {
    UUID id;
    String device_code;
    UUID area_id;
    String tenkhuvuc;
    String ten_thietbi;
    Double nguong_canh_bao;
    BigDecimal device_height;
    DeviceStatus trang_thai;
    BigDecimal lat;
    BigDecimal lon;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
