package com.example.flood_alert.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.enums.RiskLevel;

@Component
public class RiskScoreCalculator {

    public RiskLevel calculate(
            List<IoTAreaAggregates> aggregates,
            FloodPrediction prediction) {

        return RiskLevel.LOW;
    }
}