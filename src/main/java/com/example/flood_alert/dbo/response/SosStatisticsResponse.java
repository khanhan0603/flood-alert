package com.example.flood_alert.dbo.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Thống kê yêu cầu cứu hộ theo khoảng thời gian.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosStatisticsResponse {

    /**
     * Tổng số yêu cầu cứu hộ.
     */
    long totalSos;

    /**
     * Số yêu cầu đã hoàn thành.
     */
    long completedSos;

    /**
     * Số yêu cầu đang xử lý.
     */
    long processingSos;

    /**
     * Số yêu cầu đã hủy.
     */
    long cancelledSos;

    /**
     * Dữ liệu biểu đồ.
     */
    List<SosChartResponse> chart;
}