package com.example.flood_alert.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.url}")
    private String fastApiUrl;

    public boolean isFastApiHealthy() {

        try {
            restTemplate.getForObject(
                    fastApiUrl + "/health",
                    String.class);

            return true;

        } catch (Exception ex) {

            log.error("FastAPI unavailable", ex);

            return false;
        }
    }

    public void triggerPrediction() {
        if (!isFastApiHealthy()) {
            log.error("SKIP PREDICTION - FASTAPI DOWN");
            return;
        }

        log.info("START TRIGGER PREDICTION");

        String response = restTemplate.postForObject(
                fastApiUrl + "/predict-all",
                null,
                String.class);

        log.info("PREDICTION RESPONSE={}", response);
    }
}