package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.IoTDeviceCreationRequest;
import com.example.flood_alert.dbo.response.IoTDeviceCreationResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.DeviceStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.IoTDeviceRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTDeviceService {
    IoTDeviceRepository ioTDeviceRepository;
    AreaRepository areaRepository;
    UserRepository userRepository;

    public IoTDevice registerDevice(IoTDeviceCreationRequest request) {
        if (ioTDeviceRepository.findByDeviceCode(request.getDeviceCode()).isPresent())
            throw new AppException(ErrorCode.DEVICE_CODE_EXISTED);

        IoTDevice device = new IoTDevice();

        UUID areaId = Optional.ofNullable(
                areaRepository.findAreaIdByLatLon(
                        request.getLat(),
                        request.getLon()))
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));
        Area area = areaRepository.getReferenceById(areaId);

        device.setDeviceCode(request.getDeviceCode());
        device.setArea(area);
        device.setTenThietBi(request.getTenThietBi());
        device.setLat(request.getLat());
        device.setLon(request.getLon());
        device.setNguongCanhBao(request.getNguong_canh_bao());
        device.setTrangThai(DeviceStatus.PENDING);
        device.setLastSeenAt(null);
        device.setApprovedBy(null);
        device.setApprovedAt(null);
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        return ioTDeviceRepository.save(device);
    }

    public List<IoTDeviceCreationResponse> getPendingDevices() {
        return ioTDeviceRepository.findByTrangThai(DeviceStatus.PENDING)
                .stream()
                .map(device -> IoTDeviceCreationResponse.builder()
                        .id(device.getId().toString())
                        .device_code(device.getDeviceCode())
                        .area_id(device.getArea().getId().toString())
                        .tenkhuvuc(device.getArea().getTenkhuvuc())
                        .ten_thietbi(device.getTenThietBi())
                        .lat(device.getLat())
                        .lon(device.getLon())
                        .trang_thai(device.getTrangThai().name())
                        .createdAt(device.getCreatedAt().toString())
                        .updatedAt(device.getUpdatedAt().toString())
                        .build())
                .toList();
    }

    @Transactional
    public IoTDevice approveDevice(UUID deviceId, UUID adminId) {

        IoTDevice device = ioTDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        device.setTrangThai(DeviceStatus.ACTIVE);
        device.setApprovedBy(admin);
        device.setApprovedAt(LocalDateTime.now());

        return device;
    }

    @Transactional
    public void rejectDevice(UUID deviceId, UUID adminId) {

        IoTDevice device = ioTDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

        if (device.getTrangThai() != DeviceStatus.PENDING) {
            throw new AppException(ErrorCode.DEVICE_ALREADY_PROCESSED);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        device.setTrangThai(DeviceStatus.REJECTED);
        device.setApprovedBy(admin);
        device.setApprovedAt(LocalDateTime.now());
    }
}
