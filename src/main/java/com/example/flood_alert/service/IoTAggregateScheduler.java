package com.example.flood_alert.service;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IoTAggregateScheduler {
    private final IoTAreaAggregateService ioTAreaAggregateService;

    //@Scheduled(cron = "0 */2 * * * *")
    public void aggregate() {
        ioTAreaAggregateService.aggregateAllAreas();
    }
}
