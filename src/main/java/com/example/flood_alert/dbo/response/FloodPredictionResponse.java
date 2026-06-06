package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

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
    RiskLevel lead1;
    Double lead1Probability;
    RiskLevel lead2;
    Double lead2Probability;
    RiskLevel lead3;
    Double lead3Probability;
    LocalDateTime predictedAt;
    LocalDateTime weatherFrom;
    LocalDateTime weatherTo;
    String tenKhuVuc;
}
