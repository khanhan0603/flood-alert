package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.PredictionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.flood_alert.dbo.response.FloodPredictionResponse;


@RestController
@RequestMapping("/predict")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionController {
    final PredictionService predictionService;

    @GetMapping("/list")
    public List<FloodPredictionResponse> getList() {
        return predictionService.getAllPrediction();
    }
    

    @GetMapping("/list-by-area")
    public List<FloodPredictionResponse> getListByArea(@RequestParam UUID areaId) {
        return predictionService.findPredictionByArea(areaId);
    }
}
