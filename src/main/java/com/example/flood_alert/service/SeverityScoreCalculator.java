package com.example.flood_alert.service;

import org.springframework.stereotype.Component;

import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.enums.EnvironmentRisk;

@Component
public class SeverityScoreCalculator {
    public Integer calculate(
            CreateSosRequest request,
            EnvironmentRisk environmentRisk) {

        int score = 0;

        // =====================
        // S_condition
        // =====================

        if (Boolean.TRUE.equals(request.getTrapped())) {
            score += 40;
        }

        if (Boolean.TRUE.equals(request.getInjured())) {
            score += 20;
        }

        if (Boolean.TRUE.equals(request.getVulnerable())) {
            score += 10;
        }

        // =====================
        // S_victim
        // S_victim
        // Mỗi nạn nhân = 2 điểm
        // Tối đa 20 điểm (>= 10 người)
        // Nếu không giới hạn sẽ đè lên điểm của các trường hợp khẩn cấp hơn
        // =====================

        score += Math.min(
                request.getVictimCount() * 2,
                20);

        // =====================
        // S_environment
        // =====================

        switch (environmentRisk) {

            case HIGH -> score += 30;

            case MEDIUM -> score += 15;

            default -> {
            }
        }

        return score;
    }
}
