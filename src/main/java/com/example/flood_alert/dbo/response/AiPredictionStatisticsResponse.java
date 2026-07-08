package com.example.flood_alert.dbo.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Thống kê kết quả dự báo lũ của một phiên chạy AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiPredictionStatisticsResponse {

    /**
     * Tổng số khu vực đã được dự báo.
     */
    long totalAreas;

    /**
     * Số khu vực nguy cơ thấp.
     */
    long lowRiskAreas;

    /**
     * Số khu vực nguy cơ trung bình.
     */
    long mediumRiskAreas;

    /**
     * Số khu vực nguy cơ cao.
     */
    long highRiskAreas;

    /**
     * Top khu vực có xác suất lũ cao nhất.
     */
    List<HighRiskAreaResponse> topHighRiskAreas;
}