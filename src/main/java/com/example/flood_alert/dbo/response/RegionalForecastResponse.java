package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.RiskLevel;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegionalForecastResponse {

    UUID areaId;

    String tenkhuvuc;

    RiskLevel riskLevel;

    Double predictionProbability;

    RiskLevel predictionRiskLevel;

    LocalDateTime snapshotAt;
}