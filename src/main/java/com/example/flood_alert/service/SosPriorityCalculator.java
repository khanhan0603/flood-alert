package com.example.flood_alert.service;

import org.springframework.stereotype.Component;

import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;

@Component
public class SosPriorityCalculator {
    public Priority calculate(
            CreateSosRequest request,
            EnvironmentRisk environmentRisk) {

        boolean injured = request.getInjured();

        boolean trapped = request.getTrapped();

        boolean vulnerable = request.getVulnerable();

        int victimCount = request.getVictimCount();

        // =====================
        // CRITICAL
        // =====================

        if (trapped && injured) {
            return Priority.CRITICAL;
        }

        if (trapped && vulnerable) {
            return Priority.CRITICAL;
        }

        if (trapped
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.CRITICAL;
        }

        if (injured
                && vulnerable
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.CRITICAL;
        }

        // =====================
        // HIGH
        // =====================

        if (trapped) {
            return Priority.HIGH;
        }

        if (injured && vulnerable) {
            return Priority.HIGH;
        }

        if (injured
                && environmentRisk == EnvironmentRisk.HIGH) {
            return Priority.HIGH;
        }

        if (vulnerable
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

        if (injured) {
            return Priority.MEDIUM;
        }

        if (vulnerable) {
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
