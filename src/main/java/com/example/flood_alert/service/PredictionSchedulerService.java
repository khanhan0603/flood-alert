package com.example.flood_alert.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class PredictionSchedulerService {
    PredictionService predictionService;

    @Scheduled(cron="0 30 11 * * *",zone="Asia/Ho_Chi_Minh")
    public void predictMorning(){
        log.info("START MORNING PREDICTION");
        predictionService.triggerPrediction();
    }

    @Scheduled(cron="0 30 18 * * *",zone="Asia/Ho_Chi_Minh")
    public void predictEvening(){
        log.info("START EVENING PREDICTION");
        predictionService.triggerPrediction();
    }
}
