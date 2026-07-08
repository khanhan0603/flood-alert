package com.example.flood_alert.dbo.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Thống kê nguy cơ lũ hiện tại dựa trên dữ liệu AI + IoT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiIotStatisticsResponse {

    /**
     * Tổng số khu vực đã có snapshot mới nhất.
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
     * Top khu vực nguy cơ cao nhất hiện tại.
     */
    List<HighRiskSnapshotResponse> topHighRiskAreas;
}
