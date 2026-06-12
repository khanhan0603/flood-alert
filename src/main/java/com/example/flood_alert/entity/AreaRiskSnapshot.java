package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.RiskLevel;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
    name = "area_risk_snapshots",
    indexes = {
        @Index(name = "idx_snapshot_area", columnList = "area_id"),
        @Index(name = "idx_snapshot_time", columnList = "snapshot_time")
    }
)
public class AreaRiskSnapshot extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "area_id", nullable = false)
    Area area;

    @ManyToOne
    @JoinColumn(name = "aggregate_id", nullable = false)
    IoTAreaAggregates aggregate;

    @ManyToOne
    @JoinColumn(name = "prediction_id")
    FloodPrediction prediction;

    @Enumerated(EnumType.STRING)
    RiskLevel riskLevel;

    LocalDateTime snapshotTime;
}