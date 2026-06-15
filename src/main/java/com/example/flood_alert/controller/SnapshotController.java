package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.AreaRiskSnapshotResponse;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.service.AreaService;
import com.example.flood_alert.service.SnapshotService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/snapshot")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SnapshotController {
    SnapshotService snapshotService;
    AreaService areaService;

    @PostMapping("/generate/{areaId}")
    public ApiResponse<String> generateSnapshot(@PathVariable UUID areaId) {

        snapshotService.generateSnapshot(areaId);

        return ApiResponse.<String>builder()
                .result("Snapshot generated successfully")
                .build();
    }

    @PostMapping("/generate-all")
    public ApiResponse<String> generateAllSnapshots() {

        snapshotService.generateAllSnapshots();

        return ApiResponse.<String>builder()
                .result("All snapshots generated successfully")
                .build();
    }

    @GetMapping("/snapshot-lastest/{areaId}")
    public ApiResponse<AreaRiskSnapshotResponse> getAreaRiskSnapshot(@PathVariable UUID areaId) {
        AreaRiskSnapshot areaRiskSnapshot = snapshotService.getAreaRiskSnapshots(areaId);

        AreaRiskSnapshotResponse response=AreaRiskSnapshotResponse.builder()
                .areaId(areaRiskSnapshot.getArea().getId())
                .tenkhuvuc(areaService.getAreaName(areaId))
                .riskLevel(areaRiskSnapshot.getRiskLevel())
                .iotRiskScore(areaRiskSnapshot.getIotRiskScore())
                .predictionProbability(areaRiskSnapshot.getPredictionProbability())
                .dangerRatio(areaRiskSnapshot.getDangerRatio())
                .dangerDurationMinutes(areaRiskSnapshot.getDangerDurationMinutes())
                .waterRiseRatePerMinute(areaRiskSnapshot.getWaterRiseRatePerMinute())
                .dangerAggregateCount(areaRiskSnapshot.getDangerAggregateCount())
                .dangerPercent(areaRiskSnapshot.getDangerPercent())
                .predictionRiskLevel(areaRiskSnapshot.getPredictionRiskLevel())
                .snapshotAt(areaRiskSnapshot.getSnapshotAt())
                .createdAt(areaRiskSnapshot.getCreatedAt())
                .build();

        return ApiResponse.<AreaRiskSnapshotResponse>builder()
                .result(response).build();
    }
}
