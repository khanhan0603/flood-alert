package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
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
}
