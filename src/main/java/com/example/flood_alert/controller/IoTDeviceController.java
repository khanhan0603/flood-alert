package com.example.flood_alert.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.IoTDeviceCreationRequest;
import com.example.flood_alert.dbo.request.IoTReadingCreationRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.IoTDeviceDetailResponse;
import com.example.flood_alert.dbo.response.IoTReadingSensorResponse;
import com.example.flood_alert.dbo.response.NearestSensorHistoryResponse;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.service.AreaService;
import com.example.flood_alert.service.IoTAreaAggregateService;
import com.example.flood_alert.service.IoTDeviceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PutMapping;

import com.example.flood_alert.dbo.request.IoTDeviceUpdateRequest;


@Slf4j
@RestController
@RequestMapping("/iot-device")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTDeviceController {
        IoTDeviceService ioTDeviceService;
        AreaService areaService;
        IoTAreaAggregateService ioTAreaAggregateService;

        @PostMapping("/register-device")
        public ApiResponse<IoTDeviceDetailResponse> registerDevice(@RequestBody IoTDeviceCreationRequest request) {
                IoTDevice device = ioTDeviceService.registerDevice(request);

                IoTDeviceDetailResponse response = IoTDeviceDetailResponse.builder()
                                .id(device.getId())
                                .device_code(device.getDeviceCode())
                                .area_id(device.getArea().getId())
                                .tenkhuvuc(device.getArea().getTenkhuvuc())
                                .ten_thietbi(device.getTenThietBi())
                                .trang_thai(device.getTrangThai())
                                .lat(device.getLat())
                                .lon(device.getLon())
                                .nguong_canh_bao(device.getNguongCanhBao())
                                .createdAt(device.getCreatedAt())
                                .updatedAt(device.getUpdatedAt())
                                .build();

                return ApiResponse.<IoTDeviceDetailResponse>builder()
                                .result(response).build();
        }

        @GetMapping("/list-device")
        public ApiResponse<List<IoTDeviceDetailResponse>> getListDevices() {

                return ApiResponse.<List<IoTDeviceDetailResponse>>builder()
                                .result(ioTDeviceService.getListDevices())
                                .build();
        }

        @PatchMapping("/{deviceId}/approve")
        public ApiResponse<IoTDeviceDetailResponse> approveDevice(
                        @PathVariable UUID deviceId,
                        @RequestParam UUID adminId) {

                IoTDevice device = ioTDeviceService.approveDevice(
                                deviceId,
                                adminId);

                IoTDeviceDetailResponse response = IoTDeviceDetailResponse.builder()
                                .id(device.getId())
                                .device_code(device.getDeviceCode())
                                .area_id(device.getArea().getId())
                                .tenkhuvuc(device.getArea().getTenkhuvuc())
                                .ten_thietbi(device.getTenThietBi())
                                .trang_thai(device.getTrangThai())
                                .lat(device.getLat())
                                .lon(device.getLon())
                                .nguong_canh_bao(device.getNguongCanhBao())
                                .createdAt(device.getCreatedAt())
                                .updatedAt(device.getUpdatedAt())
                                .build();

                return ApiResponse.<IoTDeviceDetailResponse>builder()
                                .result(response)
                                .build();
        }

        @PatchMapping("/{deviceId}/reject")
        public ApiResponse<IoTDeviceDetailResponse> rejectDevice(
                        @PathVariable UUID deviceId,
                        @RequestParam UUID adminId) {

                IoTDevice device = ioTDeviceService.rejectDevice(deviceId, adminId);

                IoTDeviceDetailResponse response = IoTDeviceDetailResponse.builder()
                                .id(device.getId())
                                .device_code(device.getDeviceCode())
                                .area_id(device.getArea().getId())
                                .tenkhuvuc(device.getArea().getTenkhuvuc())
                                .ten_thietbi(device.getTenThietBi())
                                .trang_thai(device.getTrangThai())
                                .lat(device.getLat())
                                .lon(device.getLon())
                                .nguong_canh_bao(device.getNguongCanhBao())
                                .createdAt(device.getCreatedAt())
                                .updatedAt(device.getUpdatedAt())
                                .build();

                return ApiResponse.<IoTDeviceDetailResponse>builder()
                                .result(response)
                                .build();
        }

        @PostMapping("/read-sensor")
        public ApiResponse<IoTReadingSensorResponse> readSensorIoT(@RequestBody IoTReadingCreationRequest request) {
                IoTReadingSensorResponse response = ioTDeviceService.readSensorIoT(request);
                return ApiResponse.<IoTReadingSensorResponse>builder().result(response).build();
        }

        @PostMapping("/{areaId}")
        public String aggregate(@PathVariable UUID areaId) {
                ioTAreaAggregateService.aggregateArea(areaId);
                return "OK";
        }

        @PostMapping("/all")
        public String aggregateAll() {
                ioTAreaAggregateService.aggregateAllAreas();
                return "OK";
        }

        // Bơm dữ liệu để test
        @PostMapping("/generate-demo")
        public String generateDemo() {

                LocalDateTime from = LocalDate.now()
                                .minusDays(1)
                                .atStartOfDay();

                LocalDateTime to = LocalDateTime.now();

                ioTDeviceService.generateDemoData(
                                "ESP32_001",
                                from,
                                to);
                return "OK";
        }

        // Lấy dữ liệu 1 tiếng mực nước của device gần người dân nhất
        @GetMapping("/nearest/history")
        public ApiResponse<NearestSensorHistoryResponse> getNearestSensorHistory(

                        @RequestParam double lat,

                        @RequestParam double lon) {

                return ApiResponse
                                .<NearestSensorHistoryResponse>builder()
                                .result(
                                                ioTDeviceService.getNearestSensorHistory(
                                                                lat,
                                                                lon))
                                .build();
        }

        @PostMapping("/detail/{deviceId}")
        public ApiResponse<IoTDeviceDetailResponse>getDeviceDetail(@PathVariable UUID deviceId) {
                return ApiResponse.<IoTDeviceDetailResponse>builder()
                                .result(ioTDeviceService.getDeviceDetail(deviceId)).build();
        }

        @PutMapping("/update/{deviceId}")
        public ApiResponse<IoTDeviceDetailResponse> updateDevice(@PathVariable UUID deviceId, @RequestBody IoTDeviceUpdateRequest request) {
            return ApiResponse.<IoTDeviceDetailResponse>builder()
                            .result(ioTDeviceService.updateDevice(deviceId, request)).build();
        }
}
