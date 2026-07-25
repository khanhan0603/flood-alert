package com.example.flood_alert.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.repository.IoTReadingSensorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class IoTReadingSchedule {
    private final IoTReadingSensorRepository ioTReadingRepository;

    @Scheduled(cron = "0 00 00 * * *")
    @Transactional
    public void deleteOldIoTReadings() {

        LocalDateTime expiredTime = LocalDateTime.now().minusDays(1);
        log.info("START DELETE OLD IOT READINGS before={}", expiredTime);
        int deleted = ioTReadingRepository.deleteByRecordedAtBefore(expiredTime);
        log.info("FINISH DELETE OLD IOT READINGS deleted={}", deleted);
    }
}
