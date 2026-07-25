package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.PredictionAlertService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/predict-alert")
public class PredictionAlertController {
    private final PredictionAlertService predictionAlertService;

    @PostMapping("/prediction-alerts/{jobId}")
    public String recreateAlerts(@PathVariable UUID jobId) {

        predictionAlertService.createAlerts(jobId);

        return "OK";
    }
}
