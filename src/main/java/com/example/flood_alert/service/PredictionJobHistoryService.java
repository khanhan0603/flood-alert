package com.example.flood_alert.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.PredictionJobHistoryDetailResponse;
import com.example.flood_alert.dbo.response.PredictionJobHistoryResponse;
import com.example.flood_alert.entity.PredictionJobHistory;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.PredictionJobHistoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PredictionJobHistoryService {
    PredictionJobHistoryRepository predictionJobHistoryRepository;

    @Transactional(readOnly = true)
    public Page<PredictionJobHistoryResponse> getPredictionJobHistory(Pageable pageable) {

        return predictionJobHistoryRepository
                .findAllByOrderByStartedAtDesc(pageable)
                .map(history -> PredictionJobHistoryResponse.builder()
                        .id(history.getId().toString())
                        .date(history.getStartedAt().toLocalDate())
                        .jobType(history.getJobType())
                        .status(history.getStatus())
                        .build());
    }

    // Chi tiết 1 lần chạy
    public PredictionJobHistoryDetailResponse getPredictionJobHistoryDetail(UUID id) {

        PredictionJobHistory history = predictionJobHistoryRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PREDICTION_JOB_NOT_FOUND));

        return PredictionJobHistoryDetailResponse.builder()
                .id(history.getId().toString())

                .startedAt(history.getStartedAt())
                .finishedAt(history.getFinishedAt())

                .jobType(history.getJobType())

                .status(history.getStatus())

                .totalAreas(history.getTotalAreas())
                .processedAreas(history.getProcessedAreas())
                .highRiskAreas(history.getHighRiskAreas())
                .errors(history.getErrors())

                .recoveryAttempts(history.getRecoveryAttempts())
                .recoveredAreas(history.getRecoveredAreas())
                .remainingMissing(history.getRemainingMissing())

                .message(history.getMessage())

                .build();
    }
}
