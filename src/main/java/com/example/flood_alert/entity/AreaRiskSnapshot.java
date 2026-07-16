package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.flood_alert.enums.RiskLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "area_risk_snapshots", indexes = {
        @Index(name = "idx_snapshot_area", columnList = "area_id"),
        @Index(name = "idx_snapshot_time", columnList = "snapshotAt"),
        @Index(name = "idx_snapshot_area_time", columnList = "area_id,snapshotAt"),
        @Index(name = "idx_snapshot_risk", columnList = "riskLevel")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaRiskSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "area_id", nullable = false)
    Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id")
    FloodPrediction prediction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RiskLevel riskLevel;

    Double iotRiskScore;

    Double predictionProbability;

    // % thiết bị đang vượt ngưỡng hiện tại
    Double dangerRatio;
    //đã nguy hiểm liên tục bao lâu
    Integer dangerDurationMinutes;
    //nước đang tăng nhanh hay chậm
    BigDecimal waterRiseRatePerMinute;
    //Số lượng dữ liệu tổng hợp là nguy hiểm trong 30 phút gần nhất.
    @Column(nullable = false)
    Integer dangerAggregateCount;
    //% dữ liệu nguy hiểm trong 30 phút gần nhất.
    @Column(nullable = false)
    Double dangerPercent;

    // AI metrics
    @Enumerated(EnumType.STRING)
    RiskLevel predictionRiskLevel;

    LocalDateTime snapshotAt;

    LocalDateTime createdAt;
}