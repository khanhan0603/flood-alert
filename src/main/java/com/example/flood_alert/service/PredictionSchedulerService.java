package com.example.flood_alert.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.AiPredictionResponse;
import com.example.flood_alert.entity.PredictionJobHistory;
import com.example.flood_alert.enums.PredictionJobStatus;
import com.example.flood_alert.enums.PredictionJobType;
import com.example.flood_alert.repository.PredictionJobHistoryRepository;
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
    PredictionJobHistoryRepository predictionJobHistoryRepository;

    private static final int TOTAL_AREAS = 3321;
    private static final int BATCH_SIZE = 100;

    @Scheduled(cron = "0 30 06 * * *", zone = "Asia/Ho_Chi_Minh")
    public void predictMorning() {

        log.info("START MORNING PREDICTION");

        runBatchPrediction(PredictionJobType.MORNING);
    }

    @Scheduled(cron = "0 30 18 * * *", zone = "Asia/Ho_Chi_Minh")
    public void predictEvening() {

        log.info("START EVENING PREDICTION");

        runBatchPrediction(PredictionJobType.EVENING);
    }

    private void runBatchPrediction(PredictionJobType jobType) {

        // Thời gian bắt đầu
        LocalDateTime startedAt = LocalDateTime.now();

        int totalBatch = (TOTAL_AREAS + BATCH_SIZE - 1) / BATCH_SIZE;

        int successBatch = 0;
        int failedBatch = 0;

        int processedAreas = 0;
        int highRiskAreas = 0;
        int predictionErrors = 0;

        int recoveryAttempts = 0;
        int recoveredAreas = 0;
        int remainingMissing = 0;

        for (int offset = 0; offset < TOTAL_AREAS; offset += BATCH_SIZE) {

            log.info(
                    "START BATCH offset={} limit={}",
                    offset,
                    BATCH_SIZE);

            AiPredictionResponse response = predictionService.triggerPredictionBatch(
                    offset,
                    BATCH_SIZE);

            // Cộng dồn
            if (response != null) {

                successBatch++;

                processedAreas += response.getProcessed();
                highRiskAreas += response.getHighRisk();
                predictionErrors += response.getErrors();

                if (response.getRecovery() != null) {

                    recoveryAttempts = Math.max(
                            recoveryAttempts,
                            response.getRecovery().getAttempts());

                    recoveredAreas += response.getRecovery().getRecovered();

                    remainingMissing = response.getRecovery().getRemainingMissing();
                }

            } else {

                failedBatch++;
            }

            try {

                Thread.sleep(60 * 1000);

            }
// Lưu lịch sử tác vụ thất bại trc khi thoát
            catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                log.error("BATCH INTERRUPTED", e);

                PredictionJobHistory history = PredictionJobHistory.builder()
                        .startedAt(startedAt)
                        .finishedAt(LocalDateTime.now())

                         .jobType(jobType)

                        .totalAreas(TOTAL_AREAS)
                        .processedAreas(processedAreas)
                        .highRiskAreas(highRiskAreas)
                        .errors(predictionErrors)

                        .recoveryAttempts(recoveryAttempts)
                        .recoveredAreas(recoveredAreas)
                        .remainingMissing(remainingMissing)

                        .status(PredictionJobStatus.FAILED)
                        .message("Prediction scheduler interrupted.")

                        .build();

                try {
                    predictionJobHistoryRepository.save(history);
                } catch (Exception ex) {
                    log.error("Cannot save prediction job history", ex);
                }

                return;
            }
        }
        // Xác định trạng thái
        PredictionJobStatus status;

        String message;

        if (failedBatch == totalBatch) {

            status = PredictionJobStatus.FAILED;
            message = "Prediction job failed. Please check AI Server logs.";

        } else if (remainingMissing > 0) {

            status = PredictionJobStatus.PARTIAL_SUCCESS;
            message = String.format(
                    "Prediction completed but %d areas are still missing.",
                    remainingMissing);

        } else {

            status = PredictionJobStatus.SUCCESS;
            message = "Prediction completed successfully.";
        }

        // Tạo builder
        PredictionJobHistory history = PredictionJobHistory.builder()
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())

                 .jobType(jobType)

                .totalAreas(TOTAL_AREAS)
                .processedAreas(processedAreas)
                .highRiskAreas(highRiskAreas)
                .errors(predictionErrors)

                .recoveryAttempts(recoveryAttempts)
                .recoveredAreas(recoveredAreas)
                .remainingMissing(remainingMissing)

                .status(status)
                .message(message)

                .build();

        log.info(
                "ALL BATCHES COMPLETED success={} failed={}",
                successBatch,
                failedBatch);
        try {
            //Lưu db
            predictionJobHistoryRepository.save(history);

            log.info("Prediction job history saved.");

        } catch (Exception ex) {
            //Lỗi lưu db

            log.error("Cannot save prediction job history", ex);
        }
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Ho_Chi_Minh")
    public void cleanup() {
        int deleted = predictionRepository.deleteOldPredictions();
        log.info("Deleted {} old predictions", deleted);
    }
}
