package com.example.flood_alert.controller;

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
import com.example.flood_alert.dbo.response.IoTDeviceCreationResponse;
import com.example.flood_alert.dbo.response.IoTReadingSensorResponse;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.service.AreaService;
import com.example.flood_alert.service.IoTDeviceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/iot-device")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTDeviceController {
    IoTDeviceService ioTDeviceService;
    AreaService areaService;

    @PostMapping("/register-device")
    public ApiResponse<IoTDeviceCreationResponse> registerDevice(@RequestBody IoTDeviceCreationRequest request) {
        IoTDevice device = ioTDeviceService.registerDevice(request);

        IoTDeviceCreationResponse response = IoTDeviceCreationResponse.builder()
                .id(device.getId().toString())
                .device_code(device.getDeviceCode())
                .area_id(device.getArea().getId().toString())
                .tenkhuvuc(areaService.getAreaName(device.getArea().getId()))
                .ten_thietbi(device.getTenThietBi())
                .lat(device.getLat())
                .lon(device.getLon())
                .trang_thai(device.getTrangThai().name())
                .createdAt(device.getCreatedAt().toString())
                .updatedAt(device.getUpdatedAt().toString())
                .build();

        return ApiResponse.<IoTDeviceCreationResponse>builder()
                .result(response).build();
    }

    @GetMapping("/pending")
    public ApiResponse<List<IoTDeviceCreationResponse>> getPendingDevices() {

        return ApiResponse.<List<IoTDeviceCreationResponse>>builder()
                .result(ioTDeviceService.getPendingDevices())
                .build();
    }

    @PatchMapping("/{deviceId}/approve")
    public ApiResponse<IoTDeviceCreationResponse> approveDevice(
            @PathVariable UUID deviceId,
            @RequestParam UUID adminId) {

        IoTDevice device = ioTDeviceService.approveDevice(
                deviceId,
                adminId);

        IoTDeviceCreationResponse response = IoTDeviceCreationResponse.builder()
                .id(device.getId().toString())
                .device_code(device.getDeviceCode())
                .area_id(device.getArea().getId().toString())
                .tenkhuvuc(device.getArea().getTenkhuvuc())
                .ten_thietbi(device.getTenThietBi())
                .lat(device.getLat())
                .lon(device.getLon())
                .nguong_canh_bao(device.getNguongCanhBao())
                .trang_thai(device.getTrangThai().name())
                .createdAt(device.getCreatedAt().toString())
                .updatedAt(device.getUpdatedAt().toString())
                .build();

        return ApiResponse.<IoTDeviceCreationResponse>builder()
                .result(response)
                .build();
    }

    @PatchMapping("/{deviceId}/reject")
    public ApiResponse<IoTDeviceCreationResponse> rejectDevice(
            @PathVariable UUID deviceId,
            @RequestParam UUID adminId) {

        IoTDevice device = ioTDeviceService.rejectDevice(deviceId, adminId);

        IoTDeviceCreationResponse response = IoTDeviceCreationResponse.builder()
                .id(device.getId().toString())
                .device_code(device.getDeviceCode())
                .area_id(device.getArea().getId().toString())
                .tenkhuvuc(device.getArea().getTenkhuvuc())
                .ten_thietbi(device.getTenThietBi())
                .lat(device.getLat())
                .lon(device.getLon())
                .nguong_canh_bao(device.getNguongCanhBao())
                .trang_thai(device.getTrangThai().name())
                .createdAt(device.getCreatedAt().toString())
                .updatedAt(device.getUpdatedAt().toString())
                .build();

        return ApiResponse.<IoTDeviceCreationResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/read-sensor")
    public ApiResponse<IoTReadingSensorResponse> readSensorIoT(@RequestBody IoTReadingCreationRequest request) {
        IoTReadingSensorResponse response = ioTDeviceService.readSensorIoT(request);
        return ApiResponse.<IoTReadingSensorResponse>builder().result(response).build();
    }
}
