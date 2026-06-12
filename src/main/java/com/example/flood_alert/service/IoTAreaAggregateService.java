package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.IoTAggregateResponse;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.entity.IoTSensorReading;
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
                LocalDateTime endTime = LocalDateTime.now()
                                .withSecond(0)
                                .withNano(0);

                LocalDateTime startTime = endTime.minusMinutes(2);
                // List reading trong 2 phút
                List<IoTSensorReading> readings = ioTReadingSensorRepository
                                .findByAreaIdAndTimeRange(
                                                areaId,
                                                startTime,
                                                endTime);

                // List reading mới nhất
                List<IoTSensorReading> latestReadings = ioTReadingSensorRepository.findLatestReadingsByAreaId(areaId);

                if (latestReadings.isEmpty()) {
                        return;
                }

                log.info(
                                "AREA={} WINDOW_RECORDS={}",
                                areaId,
                                readings.size());

                if (readings.isEmpty()) {
                        return;
                }

                double minWater = readings.stream()
                                .mapToDouble(IoTSensorReading::getWaterLevel)
                                .min()
                                .orElse(0.0);

                // avgWater (trung bình mực nước mới nhất)
                double avgWater = readings.stream()
                                .mapToDouble(IoTSensorReading::getWaterLevel)
                                .average()
                                .orElse(0.0);

                // maxWater
                double maxWater = readings.stream()
                                .mapToDouble(IoTSensorReading::getWaterLevel)
                                .max()
                                .orElse(0.0);

                double currentWater = latestReadings.stream()
                                .mapToDouble(IoTSensorReading::getWaterLevel)
                                .average()
                                .orElse(0.0);

                int totalDeviceCount = latestReadings.size();

                log.info(
                                "AREA={} min={} avg={} max={} current={} devices={}",
                                areaId,
                                minWater,
                                avgWater,
                                maxWater,
                                currentWater,
                                totalDeviceCount);

                // Đếm số device vượt ngưỡng
                long dangerDeviceCount = latestReadings.stream()
                                .filter(r -> r.getDevice().getNguongCanhBao() != null
                                                && r.getWaterLevel() >= r.getDevice().getNguongCanhBao())
                                .count();

                // dangerReadingCount/totalDeviceCount
                double dangerRatio = totalDeviceCount == 0
                                ? 0.0
                                : (double) dangerDeviceCount / totalDeviceCount;

                log.info(
                                "AREA={} dangerRatio={} dangerDeviceCount={} totalDeviceCount={}",
                                areaId,
                                dangerRatio,
                                dangerDeviceCount,
                                totalDeviceCount);

                int dangerDurationMinutes = 0;

                if (dangerRatio >= 0.5) {

                        Optional<IoTAreaAggregates> previousAggregate = ioTAreaAggregateRepository
                                        .findTopByAreaIdOrderByRecordedAtDesc(areaId);

                        if (previousAggregate.isPresent()
                                        && previousAggregate.get().getDangerRatio() != null
                                        && previousAggregate.get().getDangerRatio() >= 0.5) {

                                dangerDurationMinutes = Optional.ofNullable(
                                                previousAggregate.get().getDangerDurationMinutes())
                                                .orElse(0)
                                                + 2;

                        } else {

                                dangerDurationMinutes = 2;
                        }
                }

                Map<UUID, List<IoTSensorReading>> readingsByDevice = readings.stream()
                                .collect(Collectors.groupingBy(
                                                r -> r.getDevice().getId()));

                double waterRiseRatePerMinute = readingsByDevice.values().stream()
                                .mapToDouble(deviceReadings -> {

                                        List<IoTSensorReading> sorted = deviceReadings.stream()
                                                        .sorted((r1, r2) -> r1.getRecordedAt()
                                                                        .compareTo(r2.getRecordedAt()))
                                                        .toList();

                                        if (sorted.size() < 2) {
                                                return 0.0;
                                        }

                                        double firstWater = sorted.get(0).getWaterLevel();

                                        double lastWater = sorted.get(sorted.size() - 1)
                                                        .getWaterLevel();

                                        return (lastWater - firstWater) / 2.0;
                                })
                                .average()
                                .orElse(0.0);

                log.info(
                                "AREA={} waterRiseRatePerMinute={}",
                                areaId,
                                waterRiseRatePerMinute);

                IoTAreaAggregates aggregate = IoTAreaAggregates.builder()
                                .area(areaRepository.getReferenceById(areaId))
                                .avgWater(avgWater)
                                .maxWater(maxWater)
                                .totalDeviceCount(totalDeviceCount)
                                .minWater(minWater)
                                .currentWater(currentWater)
                                .waterRiseRatePerMinute(waterRiseRatePerMinute)
                                .dangerRatio(dangerRatio)
                                .dangerDurationMinutes(dangerDurationMinutes)
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

        public List<IoTAggregateResponse> getLatestAggregateOfEachArea() {
                return ioTAreaAggregateRepository.findLatestAggregateOfEachArea()
                                .stream()
                                .map(iotAreaAggregate -> IoTAggregateResponse.builder()
                                                .area_id(iotAreaAggregate.getArea().getId())
                                                .tenkhuvuc(iotAreaAggregate.getArea().getTenkhuvuc())
                                                .avgWater(iotAreaAggregate.getAvgWater())
                                                .maxWater(iotAreaAggregate.getMaxWater())
                                                .minWater(iotAreaAggregate.getMinWater())
                                                .dangerRatio(iotAreaAggregate.getDangerRatio())
                                                .totalDeviceCount(iotAreaAggregate.getTotalDeviceCount())
                                                .currentWater(iotAreaAggregate.getCurrentWater())
                                                .waterRiseRatePerMinute(iotAreaAggregate.getWaterRiseRatePerMinute())
                                                .recordedAt(iotAreaAggregate.getRecordedAt())
                                                .build())
                                .toList();
        }

        public Page<IoTAggregateResponse> getAggregateByAreaId(
                        UUID areaId,
                        Pageable pageable) {

                Page<IoTAreaAggregates> page = ioTAreaAggregateRepository
                                .findByAreaId(areaId, pageable);

                return page.map(iotAreaAggregate -> IoTAggregateResponse.builder()
                                .area_id(iotAreaAggregate.getArea().getId())
                                .tenkhuvuc(iotAreaAggregate.getArea().getTenkhuvuc())
                                .avgWater(iotAreaAggregate.getAvgWater())
                                .maxWater(iotAreaAggregate.getMaxWater())
                                .minWater(iotAreaAggregate.getMinWater())
                                .dangerRatio(iotAreaAggregate.getDangerRatio())
                                .totalDeviceCount(iotAreaAggregate.getTotalDeviceCount())
                                .currentWater(iotAreaAggregate.getCurrentWater())
                                .waterRiseRatePerMinute(iotAreaAggregate.getWaterRiseRatePerMinute())
                                .recordedAt(iotAreaAggregate.getRecordedAt())
                                .build());
        }

}
