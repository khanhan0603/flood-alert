package com.example.flood_alert.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.flood_alert.dbo.request.IoTDeviceCreationRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.IoTDeviceCreationResponse;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.service.AreaService;
import com.example.flood_alert.service.IoTDeviceService;

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

}
