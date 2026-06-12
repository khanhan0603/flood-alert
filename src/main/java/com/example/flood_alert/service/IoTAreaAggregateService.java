package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.entity.IoTSensorReading;
import com.example.flood_alert.enums.WaterStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.IoTAreaAggregateRepository;
import com.example.flood_alert.repository.IoTReadingSensorRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class IoTAreaAggregateService {
    AreaRepository areaRepository;
    IoTReadingSensorRepository ioTReadingSensorRepository;
    IoTAreaAggregateRepository ioTAreaAggregateRepository;

    @Transactional
    public void aggregateArea(UUID areaId) {

        List<IoTSensorReading> readings = ioTReadingSensorRepository
                .findLatestReadingsByAreaId(areaId);

        if (readings.isEmpty()) {
            return;
        }

        int totalDeviceCount = readings.size();

        int safeDeviceCount = (int) readings.stream()
                .filter(r -> r.getStatus() == WaterStatus.SAFE)
                .count();

        int dangerDeviceCount = (int) readings.stream()
                .filter(r -> r.getStatus() == WaterStatus.DANGER)
                .count();

        int invalidDeviceCount = (int) readings.stream()
                .filter(r -> r.getStatus() == WaterStatus.INVALID)
                .count();

        // avgWater
        double avgWater = readings.stream()
                .filter(IoTSensorReading::getValid)
                .mapToDouble(IoTSensorReading::getWaterLevel)
                .average()
                .orElse(0.0);

        // maxWater
        double maxWater = readings.stream()
                .filter(IoTSensorReading::getValid)
                .mapToDouble(IoTSensorReading::getWaterLevel)
                .max()
                .orElse(0.0);

        double dangerRatio = totalDeviceCount == 0 ? 0 : (double) dangerDeviceCount / totalDeviceCount;

        WaterStatus iotStatus;

        // Ví dụ 10 device mà có 3 device danger thì danger
        if (dangerRatio > 0.3) {
            iotStatus = WaterStatus.DANGER;
        } else if (dangerRatio > 0) {
            iotStatus = WaterStatus.SAFE;
        } else {
            iotStatus = WaterStatus.INVALID;
        }

        IoTAreaAggregates aggregate = IoTAreaAggregates.builder()
                .area(areaRepository.findById(areaId).get())
                .avgWater(avgWater)
                .maxWater(maxWater)
                .totalDeviceCount(totalDeviceCount)
                .dangerDeviceCount(dangerDeviceCount)
                .safeDeviceCount(safeDeviceCount)
                .invalidDeviceCount(invalidDeviceCount)
                .iotStatus(iotStatus)
                .dangerRatio(dangerRatio)
                .recordedAt(LocalDateTime.now())
                .build();

        ioTAreaAggregateRepository.save(aggregate);
    }

    @Transactional
    public void aggregateAllAreas() {

        List<UUID> areaIds = areaRepository.findAllAreaIds();

        if (areaIds==null || areaIds.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_AREA);
        }

        for (UUID areaId : areaIds) {
            try {
                aggregateArea(areaId);
            } catch (Exception e) {
                log.error(
                        "Aggregate failed for area={}",
                        areaId,
                        e);
            }
        }
    }
}
