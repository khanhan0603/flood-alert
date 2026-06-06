package com.example.flood_alert.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.flood_alert.dbo.response.FloodPredictionResponse;
import com.example.flood_alert.repository.PredictionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionService {

    final RestTemplate restTemplate;
    final PredictionRepository predictionReposiory;

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

    public List<FloodPredictionResponse> getAllPrediction() {
        return predictionReposiory.findAllPrediction();
    }

    public List<FloodPredictionResponse> findPredictionByArea(UUID areaId) {
        return predictionReposiory.findPredictionByArea(areaId);
    }
}