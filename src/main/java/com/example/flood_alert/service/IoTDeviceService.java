package com.example.flood_alert.service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.request.IoTDeviceCreationRequest;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.enums.DeviceStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.IoTDeviceRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTDeviceService {
    IoTDeviceRepository ioTDeviceRepository;
    AreaRepository areaRepository;

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
        device.setNguongCanhBao(0.0);
        device.setTrangThai(DeviceStatus.PENDING);
        device.setLastSeenAt(null);
        device.setApprovedBy(null);
        device.setApprovedAt(null);
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        return ioTDeviceRepository.save(device);
    }
}
