package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.PredictionJobStatus;
import com.example.flood_alert.enums.PredictionJobType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionJobHistoryDetailResponse {

    String id;

    LocalDateTime startedAt;
    LocalDateTime finishedAt;

    PredictionJobType jobType;

    PredictionJobStatus status;

    Integer totalAreas;
    Integer processedAreas;
    Integer highRiskAreas;
    Integer errors;

    Integer recoveryAttempts;
    Integer recoveredAreas;
    Integer remainingMissing;

    String message;
}