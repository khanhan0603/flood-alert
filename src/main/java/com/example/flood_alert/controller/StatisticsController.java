package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.AiPredictionStatisticsResponse;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.OverviewStatisticsResponse;
import com.example.flood_alert.service.StatisticsService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Cung cấp các API thống kê và báo cáo dành cho Admin.
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsController {

    StatisticsService statisticsService;

    /**
     * API lấy dữ liệu thống kê tổng quan của hệ thống.
     *
     * Frontend gọi API này khi tải Dashboard để hiển thị
     * các thẻ thống kê tổng quan.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<OverviewStatisticsResponse> getOverview() {

        return ApiResponse.<OverviewStatisticsResponse>builder()
                .message("Lấy thống kê tổng quan thành công.")
                .result(statisticsService.getOverview())
                .build();
    }

    /**
     * Thống kê kết quả dự báo lũ theo một phiên chạy AI.
     *
     * @param predictionJobHistoryId Id phiên chạy AI.
     * @return Thống kê dự báo lũ.
     */
    @GetMapping("/ai-predictions/{predictionJobHistoryId}")
    public ApiResponse<AiPredictionStatisticsResponse> getAiPredictionStatistics(
            @PathVariable UUID predictionJobHistoryId) {

        return ApiResponse.<AiPredictionStatisticsResponse>builder()
                .message("Lấy thống kê dự báo lũ thành công.")
                .result(statisticsService.getAiPredictionStatistics(
                        predictionJobHistoryId))
                .build();
    }
}