package com.example.flood_alert.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.repository.IoTReadingSensorRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IoTReadingSchedule {
    private final IoTReadingSensorRepository ioTReadingRepository;
    @Scheduled(cron = "00 30 14 * * *")
    @Transactional
    public void deleteOldIoTReadings() {
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(1);
        ioTReadingRepository.deleteByRecordedAtBefore(expiredTime);
    }
}
