package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.WeatherDataInitializerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/weather-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class WeatherDataController {
    WeatherDataInitializerService weatherDataInitializerService;

    @PostMapping("/backfill")
    public String backfill(
            @RequestParam(defaultValue = "2") int limit
    ) {

        weatherDataInitializerService.backfill(limit);

        return "DONE";
    }
}
