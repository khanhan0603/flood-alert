package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.IoTDeviceCreationRequest;
import com.example.flood_alert.dbo.request.IoTReadingCreationRequest;
import com.example.flood_alert.dbo.response.IoTDeviceCreationResponse;
import com.example.flood_alert.dbo.response.IoTReadingSensorResponse;
import com.example.flood_alert.dbo.response.NearestSensorHistoryResponse;
import com.example.flood_alert.dbo.response.SensorWaterHistoryResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.DeviceAlert;
import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.entity.IoTSensorReading;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.DeviceStatus;
import com.example.flood_alert.enums.WaterStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.DeviceAlertRepository;
import com.example.flood_alert.repository.IoTDeviceRepository;
import com.example.flood_alert.repository.IoTReadingSensorRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IoTDeviceService {
        IoTDeviceRepository ioTDeviceRepository;
        AreaRepository areaRepository;
        UserRepository userRepository;
        IoTReadingSensorRepository ioTReadingSensorRepository;
        DeviceAlertRepository deviceAlertRepository;
        IoTAreaAggregateService ioTAreaAggregateService;
        IoTReadingGeneratorService ioTReadingGeneratorService;

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
                device.setConsecutiveInvalidCount(0);
                return ioTDeviceRepository.save(device);
        }

        public List<IoTDeviceCreationResponse> getListDevices() {
                return ioTDeviceRepository.getListOrderByTrangThai()
                                .stream()
                                .map(device -> IoTDeviceCreationResponse.builder()
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
                                                .build())
                                .toList();
        }

        @Transactional
        public IoTDevice approveDevice(UUID deviceId, UUID adminId) {

                IoTDevice device = ioTDeviceRepository.findById(deviceId)
                                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

                if (device.getTrangThai() != DeviceStatus.PENDING) {
                        throw new AppException(ErrorCode.DEVICE_ALREADY_PROCESSED);
                }

                User admin = userRepository.findById(adminId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                device.setTrangThai(DeviceStatus.ACTIVE);
                device.setApprovedBy(admin);
                device.setApprovedAt(LocalDateTime.now());

                return device;
        }

        @Transactional
        public IoTDevice rejectDevice(UUID deviceId, UUID adminId) {

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

                return device;
        }

        // Nhận dữ liệu từ IoT
        @Transactional
        public IoTReadingSensorResponse readSensorIoT(
                        IoTReadingCreationRequest request) {
                IoTDevice device = ioTDeviceRepository
                                .findByDeviceCode(request.getDeviceCode())
                                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

                IoTSensorReading reading = new IoTSensorReading();
                if (device.getTrangThai() == DeviceStatus.PENDING) {
                        throw new AppException(ErrorCode.DEVICE_PENDING);
                }
                if (device.getTrangThai() == DeviceStatus.REJECTED) {
                        throw new AppException(ErrorCode.DEVICE_REJECTED);
                }
                reading.setDevice(device);
                reading.setWaterLevel(request.getWaterLevel());

                // update last seen
                device.setLastSeenAt(LocalDateTime.now());

                Double waterLevel = request.getWaterLevel();

                // validate dữ liệu
                if (waterLevel == null
                                || waterLevel <= 0
                                || waterLevel > 14) {

                        reading.setValid(false);
                        reading.setStatus(WaterStatus.INVALID);
                        device.setConsecutiveInvalidCount(device.getConsecutiveInvalidCount() + 1);
                } else {

                        reading.setValid(true);

                        device.setConsecutiveInvalidCount(0);

                        if (device.getTrangThai() == DeviceStatus.ERROR
                                        || device.getTrangThai() == DeviceStatus.INACTIVE) {
                                device.setTrangThai(DeviceStatus.ACTIVE);
                        }

                        Double threshold = device.getNguongCanhBao();

                        if (threshold != null && waterLevel >= threshold) {
                                reading.setStatus(WaterStatus.DANGER);
                        } else {
                                reading.setStatus(WaterStatus.SAFE);
                        }
                }

                if (device.getConsecutiveInvalidCount() >= 3 && device.getTrangThai() != DeviceStatus.ERROR) {
                        device.setTrangThai(DeviceStatus.ERROR);
                        sendAlert(device);
                }

                reading.setRecordedAt(
                                Optional.ofNullable(request.getRecordedAt())
                                                .orElse(LocalDateTime.now()));

                IoTSensorReading savedReading = ioTReadingSensorRepository.save(reading);

                return IoTReadingSensorResponse.builder()
                                .device_code(savedReading.getDevice().getDeviceCode())
                                .device_id(savedReading.getDevice().getId().toString())
                                .recorded_at(savedReading.getRecordedAt())
                                .build();
        }

        // Sinh dữ liệu IoT để demo
        public void generateDemoData(
                        String deviceCode,
                        LocalDateTime from,
                        LocalDateTime to) {

                IoTDevice device = ioTDeviceRepository
                                .findByDeviceCode(deviceCode)
                                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

                if (device.getArea() == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                log.info("=== BEFORE saveReadings ===");
                ioTReadingGeneratorService.saveReadings(device, from, to);
                log.info("=== AFTER saveReadings ===");

                log.info("=== BEFORE generateAggregateData ===");
                ioTAreaAggregateService.generateAggregateData(
                                device.getArea().getId(),
                                from,
                                to);
                log.info("=== AFTER generateAggregateData ===");
        }

        // Cảnh báo dữ liệu bất thường
        public void sendAlert(IoTDevice device) {
                DeviceAlert alert = DeviceAlert.builder()
                                .iotDevice(device)
                                .message(
                                                "Thiết bị " + device.getDeviceCode() +
                                                                " gửi dữ liệu bất thường liên tiếp 3 lần")
                                .resolved(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                log.warn(
                                "DEVICE ERROR: {}",
                                device.getDeviceCode());
                deviceAlertRepository.save(alert);
        }

        // Lấy dữ liệu bất thường nhat cơ bản theo device gần người dân
        @Transactional(readOnly = true)
        public NearestSensorHistoryResponse getNearestSensorHistory(
                        double lat,
                        double lon) {

                // Tìm thiet bị gần khu vực người dân nhất
                IoTDevice device = ioTDeviceRepository
                                .findNearestDevice(lat, lon)
                                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND));

                // Tính khoảng cách từ device gần nhất đến người dân
                Double distanceMeters = ioTDeviceRepository
                                .calculateDistance(
                                                device.getId(),
                                                lat,
                                                lon);

                // Lấy dữ liệu bất thường nhat cơ bản theo device gần người dân
                List<SensorWaterHistoryResponse> histories = ioTReadingSensorRepository
                                .findByDeviceIdOrderByRecordedAtDesc(
                                                device.getId(),
                                                PageRequest.of(0, 360)) // 1 giờ dữ liệu tại 10s lấy 1 lần
                                .stream()
                                .map(reading -> SensorWaterHistoryResponse.builder()
                                                .waterLevel(reading.getWaterLevel())
                                                .recordedAt(reading.getRecordedAt())
                                                .build())
                                .toList();

                return NearestSensorHistoryResponse.builder()
                                .deviceId(device.getId())
                                .deviceName(device.getTenThietBi())
                                .distanceMeters(distanceMeters)
                                .histories(histories)
                                .build();
        }

}
