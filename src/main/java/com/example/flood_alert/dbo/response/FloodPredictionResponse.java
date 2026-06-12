package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.RiskLevel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloodPredictionResponse {
    UUID area_id;
    String tenKhuVuc;
    RiskLevel lead1;
    Double lead1Probability;
    RiskLevel lead2;
    Double lead2Probability;
    RiskLevel lead3;
    Double lead3Probability;
    LocalDateTime predictedAt;
    LocalDateTime weatherFrom;
    LocalDateTime weatherTo;
}
