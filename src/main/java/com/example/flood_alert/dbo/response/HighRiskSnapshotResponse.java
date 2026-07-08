package com.example.flood_alert.dbo.response;

import com.example.flood_alert.enums.RiskLevel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HighRiskSnapshotResponse {

    /**
     * Tên khu vực.
     */
    String areaName;

    /**
     * Mức độ nguy cơ tổng hợp cuối cùng
     * sau khi kết hợp AI và IoT.
     */
    RiskLevel riskLevel;

    /**
     * Xác suất dự báo lũ của AI (0.0 - 1.0).
     */
    Double predictionProbability;

    /**
     * Tỷ lệ thiết bị IoT đang vượt ngưỡng cảnh báo (%).
     */
    Double dangerRatio;

    /**
     * Tốc độ mực nước tăng trung bình
     * (cm/phút).
     */
    Double waterRiseRatePerMinute;

}
