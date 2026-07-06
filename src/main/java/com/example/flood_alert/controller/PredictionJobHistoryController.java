package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.PredictionJobHistoryDetailResponse;
import com.example.flood_alert.dbo.response.PredictionJobHistoryResponse;
import com.example.flood_alert.service.PredictionJobHistoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/prediction-jobs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PredictionJobHistoryController {

    PredictionJobHistoryService predictionJobHistoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<Page<PredictionJobHistoryResponse>> getPredictionJobs(
            @PageableDefault(size = 10) Pageable pageable) {

        return ApiResponse.<Page<PredictionJobHistoryResponse>>builder()
                .result(
                        predictionJobHistoryService.getPredictionJobHistory(pageable))
                .build();
    }

    // Chi tiết 1 lần chạy
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<PredictionJobHistoryDetailResponse> getPredictionJobHistoryDetail(
            @PathVariable UUID id) {

        return ApiResponse.<PredictionJobHistoryDetailResponse>builder()
                .result(
                        predictionJobHistoryService.getPredictionJobHistoryDetail(id))
                .build();
    }
}