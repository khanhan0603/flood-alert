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
import com.example.flood_alert.repository.IoTDeviceRepository;
import com.example.flood_alert.repository.IoTReadingSensorRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTAreaAggregateService {
    AreaRepository areaRepository;
    IoTReadingSensorRepository ioTReadingSensorRepository;
    IoTAreaAggregateRepository ioTAreaAggregateRepository;
    IoTDeviceRepository ioTDeviceRepository;

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
        ;

        // avgWater (trung bình mực nước mới nhất)
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
        if (dangerRatio >= 0.3) {
            iotStatus = WaterStatus.DANGER;
        } else if (safeDeviceCount > 0) {
            iotStatus = WaterStatus.SAFE;
        } else {
            iotStatus = WaterStatus.INVALID;
        }

        IoTAreaAggregates aggregate = IoTAreaAggregates.builder()
                .area(areaRepository.getReferenceById(areaId))
                .avgWater(avgWater)
                .maxWater(maxWater)
                .totalDeviceCount(totalDeviceCount)
                .dangerDeviceCount(dangerDeviceCount)
                .safeDeviceCount(safeDeviceCount)
                .iotStatus(iotStatus)
                .dangerRatio(dangerRatio)
                .recordedAt(LocalDateTime.now())
                .build();

        ioTAreaAggregateRepository.save(aggregate);
    }

    public void aggregateAllAreas() {
        log.info("START IOT AGGREGATE");

        List<UUID> areaIds = ioTDeviceRepository.findAreaIdsHasActiveDevice();

        log.info(
                "TOTAL AREA HAS ACTIVE DEVICE={}",
                areaIds.size());

        if (areaIds.isEmpty()) {
            log.info("NO ACTIVE DEVICE FOUND");
            return;
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
        log.info("FINISH IOT AGGREGATE");
    }
}
