package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.IoTAggregateResponse;
import com.example.flood_alert.service.IoTAreaAggregateService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/iot-aggregate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTAreaAggregateController {
        IoTAreaAggregateService ioTAreaAggregateService;

        //Danh sách các bản ghi mực nước tổng hợp mới nhất của các phường/xã có IoT
        @GetMapping
        public ApiResponse<List<IoTAggregateResponse>> getLatestAggregates() {

                return ApiResponse.<List<IoTAggregateResponse>>builder()
                                .result(
                                                ioTAreaAggregateService
                                                                .getLatestAggregateOfEachArea())
                                .build();
        }

        //Danh sách các dữ liệu mực nước tổng hợp của 1 khu vực
        @GetMapping("/{areaId}")
        public ApiResponse<Page<IoTAggregateResponse>> getAggregateByAreaId(
                        @PathVariable UUID areaId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by("recordedAt").descending());

                return ApiResponse.<Page<IoTAggregateResponse>>builder()
                                .result(
                                                ioTAreaAggregateService
                                                                .getAggregateByAreaId(
                                                                                areaId,
                                                                                pageable))
                                .build();
        }

        //Bản ghi tổng hợp mực nước mới nhất của 1 khu vực
        @GetMapping("/areas/{areaId}/latest")
        public ApiResponse<IoTAggregateResponse> getLatestAggregate(
                        @PathVariable UUID areaId) {

                return ApiResponse.<IoTAggregateResponse>builder()
                                .result(ioTAreaAggregateService.getLatestAggregate(areaId))
                                .build();
        }
}
