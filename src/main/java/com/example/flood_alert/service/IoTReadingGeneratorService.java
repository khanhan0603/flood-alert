package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.entity.IoTSensorReading;
import com.example.flood_alert.enums.WaterStatus;
import com.example.flood_alert.repository.IoTReadingSensorRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IoTReadingGeneratorService {

    IoTReadingSensorRepository ioTReadingSensorRepository;

    private static final double MIN_WATER_LEVEL = 0.0;
    private static final double MAX_WATER_LEVEL = 14.0;

    @Transactional
    public void saveReadings(IoTDevice device, LocalDateTime from, LocalDateTime to) {
        log.info("saveReadings START device={} from={} to={}", device.getDeviceCode(), from, to);

        List<IoTSensorReading> readings = new ArrayList<>();
        LocalDateTime current = from;
        long index = 0;

        while (!current.isAfter(to)) {
            double water = generateWaterLevel(current, index);

            // Chỉ nhận dữ liệu hợp lệ: (0, 14], nếu dữ liệu ko hợp lệ sẽ bỏ qua 
            //code kế mà đi tới lần tiếp theo.

            if (water <= MIN_WATER_LEVEL || water > MAX_WATER_LEVEL) {
                log.warn("Discard invalid reading. device={} waterLevel={}",
                        device.getDeviceCode(), water);
                current = current.plusSeconds(10);
                index++;
                continue;
            }

            readings.add(IoTSensorReading.builder()
                    .device(device)
                    .waterLevel(water)
                    .valid(true)
                    .status(water >= device.getNguongCanhBao() ? WaterStatus.DANGER : WaterStatus.SAFE)
                    .recordedAt(current)
                    .build());

            current = current.plusSeconds(10);
            index++;
        }

        log.info("saveReadings built {} readings", readings.size());
        ioTReadingSensorRepository.saveAll(readings);
        log.info("saveReadings DONE");
    }

    private double generateWaterLevel(
            LocalDateTime time,
            long index) {

        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

        double baseWater = 5;
        double naturalWave = Math.sin(index / 3000.0) * 2;
        double noise = random.nextDouble(-0.3, 0.3);
        double floodBoost = 0;

        int hour = time.getHour();
        if (hour >= 14 && hour <= 18) {
            floodBoost = 8;
        } else if (hour >= 7 && hour <= 10) {
            floodBoost = 4;
        }

        return Math.round(Math.max(0, baseWater + naturalWave + noise + floodBoost) * 100.0) / 100.0;
    }
}