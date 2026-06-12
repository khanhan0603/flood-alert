package com.example.flood_alert.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.flood_alert.repository.PredictionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PredictionSchedulerService {

    PredictionService predictionService;
    PredictionRepository predictionRepository;

    private static final int TOTAL_AREAS = 3321;
    private static final int BATCH_SIZE = 500;

    @Scheduled(cron = "0 30 06 * * *", zone = "Asia/Ho_Chi_Minh")
    public void predictMorning() {

        log.info("START MORNING PREDICTION");

        runBatchPrediction();
    }

    @Scheduled(cron = "0 00 19 * * *", zone = "Asia/Ho_Chi_Minh")
    public void predictEvening() {

        log.info("START EVENING PREDICTION");

        runBatchPrediction();
    }

    private void runBatchPrediction() {

        for (int offset = 0; offset < TOTAL_AREAS; offset += BATCH_SIZE) {

            log.info(
                    "START BATCH offset={} limit={}",
                    offset,
                    BATCH_SIZE
            );

            predictionService.triggerPredictionBatch(
                    offset,
                    BATCH_SIZE
            );

            try {

                Thread.sleep(60 * 1000);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                log.error("BATCH INTERRUPTED", e);

                return;
            }
        }

        log.info("ALL BATCHES COMPLETED");
    }

    @Scheduled(cron="0 0 2 * * *",zone="Asia/Ho_Chi_Minh")
    public void cleanup(){
        int deleted=predictionRepository.deleteOldPredictions();
        log.info("Deleted {} old predictions", deleted);
    }
}
