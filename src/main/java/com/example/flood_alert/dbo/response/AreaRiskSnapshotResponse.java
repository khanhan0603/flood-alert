package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.RiskLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaRiskSnapshotResponse {
    UUID areaId;
    String tenkhuvuc;
    RiskLevel riskLevel;
    Double iotRiskScore;
    Double predictionProbability;
    Double dangerRatio;
    Integer dangerDurationMinutes;
    Double waterRiseRatePerMinute;
    Integer dangerAggregateCount;
    Double dangerPercent;
    RiskLevel predictionRiskLevel;
    LocalDateTime snapshotAt;
    LocalDateTime createdAt;
}
