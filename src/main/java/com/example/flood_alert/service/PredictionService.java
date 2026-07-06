package com.example.flood_alert.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.flood_alert.dbo.response.AiPredictionResponse;
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
    final PredictionRepository predictionRepository;

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

    public AiPredictionResponse triggerPredictionBatch(int offset, int limit) {

        if (!isFastApiHealthy()) {
            log.error(
                    "SKIP BATCH offset={} limit={} - FASTAPI DOWN",
                    offset,
                    limit);
            return null;
        }

        try {
            ResponseEntity<AiPredictionResponse> response = restTemplate.postForEntity(
                    fastApiUrl +
                            "/predict-batch?offset=" +
                            offset +
                            "&limit=" +
                            limit,
                    HttpEntity.EMPTY,
                    AiPredictionResponse.class);

            log.info(
                    "BATCH offset={} limit={} status={} response={}",
                    offset,
                    limit,
                    response.getStatusCode(),
                    response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            return response.getBody();
        } catch (RestClientException ex) {
            log.error("BATCH FAILED offset={} limit={}", offset, limit, ex);
            return null;
        }
    }

    // Thêm endpoint để Spring poll trạng thái nếu cần
    public String getPredictionStatus() {
        return restTemplate.getForObject(fastApiUrl + "/predict-all/status", String.class);
    }

    public List<FloodPredictionResponse> getAllPrediction() {
        return predictionRepository.findLatestPredictionsForAllAreas();
    }

    public List<FloodPredictionResponse> findPredictionByArea(UUID areaId) {
        return predictionRepository.findPredictionByArea(areaId);
    }

    // Recovery after prediction
    public void triggerRecovery() {

        if (!isFastApiHealthy()) {
            log.error("SKIP RECOVERY - FASTAPI DOWN");
            return;
        }

        try {

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/recover-missing",
                    HttpEntity.EMPTY,
                    String.class);

            log.info(
                    "RECOVERY status={} body={}",
                    response.getStatusCode(),
                    response.getBody());

        } catch (RestClientException ex) {

            log.error("RECOVERY FAILED", ex);
        }
    }
}
