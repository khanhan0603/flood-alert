package com.example.flood_alert.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.flood_alert.entity.EmergencyCallEvent;
import com.example.flood_alert.enums.CallEventStatus;
import com.example.flood_alert.repository.EmergencyCallEventRepository;

import lombok.RequiredArgsConstructor;

//5 phút mà không tạo sos reuqest thì dữ liệu call event sẽ bị xem như spam, người
//dân ko yêu cầu sos -> chuyển về trạng thái STALE
@Component
@RequiredArgsConstructor
public class EmergencyCallEventCleanupScheduler {

    private final EmergencyCallEventRepository repository;

    @Scheduled(fixedDelay = 300000) // 5 phút
    public void cleanupStaleEvents() {

        LocalDateTime expiredTime = LocalDateTime.now().minus(60, ChronoUnit.MINUTES);

        List<EmergencyCallEvent> events =
                repository.findByStatusAndCreatedAtBefore(
                        CallEventStatus.PENDING_MATCH,
                        expiredTime);

        for (EmergencyCallEvent event : events) {
            event.setStatus(CallEventStatus.STALE);
        }

        repository.saveAll(events);
    }
}