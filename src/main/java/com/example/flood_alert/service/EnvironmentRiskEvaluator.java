package com.example.flood_alert.service;

import org.springframework.stereotype.Component;

import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.enums.EnvironmentRisk;

@Component
public class EnvironmentRiskEvaluator {
    //Ngưỡng mực nước cực đoan: tăng 10cm/phút
    private static final double EXTREME_WATER_RISE = 10.0;

    // Ngưỡng thời gian danger cực đoan: 90 phút
    private static final int EXTREME_DANGER_DURATION = 90;

    public EnvironmentRisk evaluate(
            AreaRiskSnapshot snapshot) {

        if (snapshot == null) {
            return EnvironmentRisk.LOW;
        }

        // Escalate khi cực đoan
        if ((snapshot.getWaterRiseRatePerMinute() != null && snapshot.getWaterRiseRatePerMinute() >= EXTREME_WATER_RISE)
                || (snapshot.getDangerDurationMinutes() != null
                        && snapshot.getDangerDurationMinutes() >= EXTREME_DANGER_DURATION)) {

            return EnvironmentRisk.HIGH;
        }

        return switch (snapshot.getRiskLevel()) {

            case HIGH -> EnvironmentRisk.HIGH;

            case MEDIUM -> EnvironmentRisk.MEDIUM;

            default -> EnvironmentRisk.LOW;
        };
    }
}