package com.example.flood_alert.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SnapshotSchedule {
    private final SnapshotService snapshotService;


    @Scheduled(cron="0 */15 * * * *")
    public void generateSnapshots(){
        snapshotService.generateAllSnapshots();
    }
}
