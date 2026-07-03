package com.example.flood_alert.service;

import org.springframework.stereotype.Component;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;

@Component
public class SosPriorityCalculator {
    public Priority calculate(
            Integer victimCount,
            Boolean injured,
            Boolean trapped,
            Boolean vulnerable,
            EnvironmentRisk environmentRisk) {
        // =====================
        // CRITICAL
        // =====================

        if (Boolean.TRUE.equals(trapped)
                && Boolean.TRUE.equals(injured)) {
            return Priority.CRITICAL;
        }

        if (Boolean.TRUE.equals(trapped)
                && Boolean.TRUE.equals(vulnerable)) {
            return Priority.CRITICAL;
        }

        if (Boolean.TRUE.equals(trapped)
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.CRITICAL;
        }

        if (Boolean.TRUE.equals(injured)
                && Boolean.TRUE.equals(vulnerable)
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.CRITICAL;
        }
        // =====================
        // HIGH
        // =====================

        if (Boolean.TRUE.equals(trapped)) {
            return Priority.HIGH;
        }

        if (Boolean.TRUE.equals(injured)
                && Boolean.TRUE.equals(vulnerable)) {
            return Priority.HIGH;
        }

        if (Boolean.TRUE.equals(injured)
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.HIGH;
        }

        if (Boolean.TRUE.equals(vulnerable)
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.HIGH;
        }

        if (victimCount >= 5
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.HIGH;
        }

        // =====================
        // MEDIUM
        // =====================

        if (Boolean.TRUE.equals(injured)) {
            return Priority.MEDIUM;
        }

        if (Boolean.TRUE.equals(vulnerable)) {
            return Priority.MEDIUM;
        }

        if (victimCount >= 10) {
            return Priority.MEDIUM;
        }

        if (environmentRisk == EnvironmentRisk.MEDIUM) {
            return Priority.MEDIUM;
        }

        // =====================
        // LOW
        // =====================

        return Priority.LOW;
    }
}
