package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.PredictRequest;
import com.example.flood_alert.dbo.response.AiPredictionResponse;
import com.example.flood_alert.dbo.response.FloodPredictionResponse;
import com.example.flood_alert.service.PredictionSchedulerService;
import com.example.flood_alert.service.PredictionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/predict")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionController {
    final PredictionService predictionService;
    private final PredictionSchedulerService predictionSchedulerService;

    @PostMapping("/run")
    public ResponseEntity<String> runPrediction() {

        predictionSchedulerService.runManualPrediction();

        return ResponseEntity.ok("Prediction started.");
    }

    @PostMapping("/run-test")
    public ResponseEntity<AiPredictionResponse> runPredictionTestArea(
            @RequestBody PredictRequest request) {

        AiPredictionResponse response = predictionService.triggerPredictionTestBatch(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-by-area")
    public List<FloodPredictionResponse> getListByArea(@RequestParam UUID areaId) {
        return predictionService.findPredictionByArea(areaId);
    }
}
