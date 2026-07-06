package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateHotlineSosRequest {

    // ===== Chỉ dùng khi SOS còn PENDING =====

    BigDecimal lat;

    BigDecimal lon;

    Integer victimCount;

    Boolean injured;

    Boolean trapped;

    Boolean vulnerable;

    // ===== Được phép sửa cả khi ASSIGNED/PROCESSING =====

    String sodt;

    String diaChi;

    String mota;
}