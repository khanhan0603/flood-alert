package com.example.flood_alert.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.AiIotStatisticsResponse;
import com.example.flood_alert.dbo.response.AiPredictionStatisticsResponse;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.OverviewStatisticsResponse;
import com.example.flood_alert.enums.PredictionJobType;
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
     * Lấy thống kê dự báo lũ của phiên AI mới nhất.
     */
    @GetMapping("/ai-predictions/latest")
    public ApiResponse<AiPredictionStatisticsResponse> getLatestAiPredictionStatistics() {

        return ApiResponse.<AiPredictionStatisticsResponse>builder()
                .message("Lấy thống kê dự báo lũ mới nhất thành công.")
                .result(statisticsService.getLatestAiPredictionStatistics())
                .build();
    }

    /**
     * Lấy thống kê dự báo lũ theo ngày và ca chạy.
     */
    @GetMapping("/ai-predictions")
    public ApiResponse<AiPredictionStatisticsResponse> getAiPredictionStatistics(
            @RequestParam LocalDate date,
            @RequestParam PredictionJobType jobType) {

        return ApiResponse.<AiPredictionStatisticsResponse>builder()
                .message("Lấy thống kê dự báo lũ thành công.")
                .result(statisticsService.getAiPredictionStatistics(
                        date,
                        jobType))
                .build();
    }

    /**
     * Thống kê nguy cơ lũ hiện tại dựa trên dữ liệu AI + IoT.
     */
    @GetMapping("/ai-iot")
    public ApiResponse<AiIotStatisticsResponse> getAiIotStatistics() {

        return ApiResponse.<AiIotStatisticsResponse>builder()
                .message("Lấy thống kê AI + IoT thành công.")
                .result(statisticsService.getAiIotStatistics())
                .build();
    }
}