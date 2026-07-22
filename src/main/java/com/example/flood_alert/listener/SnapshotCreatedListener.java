package com.example.flood_alert.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.event.SnapshotCreatedEvent;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotCreatedListener {

    private final AreaRiskSnapshotRepository areaRiskSnapshotRepository;
    private final AlertService alertService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SnapshotCreatedEvent event) {

        log.info("Receive SnapshotCreatedEvent: {}", event.snapshotId());

        AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                .findById(event.snapshotId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Snapshot not found: " + event.snapshotId()));

        alertService.processSnapshot(snapshot);
    }
}