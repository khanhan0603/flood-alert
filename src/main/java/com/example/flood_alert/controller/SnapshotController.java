package com.example.flood_alert.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import org.springframework.web.bind.annotation.RequestParam;

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

        AreaRiskSnapshotResponse response = AreaRiskSnapshotResponse.builder()
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

    @GetMapping("/list-snapshot-by-areaId/{areaId}")
    public ApiResponse<Page<AreaRiskSnapshotResponse>> getAreaRiskSnapshot(@PathVariable UUID areaId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime snapBegin = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .atStartOfDay();

        LocalDateTime snapEnd = snapBegin.plusDays(1);

        return ApiResponse.<Page<AreaRiskSnapshotResponse>>builder()
                .result(snapshotService.getListSnapshotByAreaId(areaId, snapBegin, snapEnd, pageable))
                .build();
    }

}
