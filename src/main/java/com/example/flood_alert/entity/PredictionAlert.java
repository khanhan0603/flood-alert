package com.example.flood_alert.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.example.flood_alert.enums.AlertStatus;
import com.example.flood_alert.enums.RiskLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prediction_alerts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionAlert {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_job_history_id", nullable = false)
    PredictionJobHistory predictionJobHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    Area area;

    @Column(name = "prediction_date", nullable = false)
    Date predictionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RiskLevel riskLevel;

    @Column(nullable = false, length = 255)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AlertStatus status;

    @Column(nullable = false)
    LocalDateTime createdAt;
}