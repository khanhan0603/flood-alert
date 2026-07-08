package com.example.flood_alert.dbo.response;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Dữ liệu biểu đồ SOS theo ngày.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosChartResponse {

    /**
     * Ngày thống kê.
     */
    LocalDate date;

    /**
     * Số lượng SOS trong ngày.
     */
    long totalSos;

}