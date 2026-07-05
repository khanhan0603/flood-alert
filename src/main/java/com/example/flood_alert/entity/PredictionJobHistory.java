package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.PredictionJobStatus;
import com.example.flood_alert.enums.PredictionJobType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "prediction_job_history", indexes = {
        @Index(name = "idx_prediction_job_history_started_at", columnList = "started_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionJobHistory extends BaseEntity {

    @Column(nullable = false)
    LocalDateTime startedAt;

    LocalDateTime finishedAt;

    @Column(nullable = false)
    Integer totalAreas;

    @Column(nullable = false)
    Integer processedAreas;

    @Column(nullable = false)
    Integer highRiskAreas;

    @Column(nullable = false)
    Integer errors;

    @Column(nullable = false)
    Integer recoveryAttempts;

    @Column(nullable = false)
    Integer recoveredAreas;

    @Column(nullable = false)
    Integer remainingMissing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PredictionJobStatus status;

    @Column(length = 1000)
    String message;

    @Enumerated(EnumType.STRING)
    PredictionJobType jobType;
}