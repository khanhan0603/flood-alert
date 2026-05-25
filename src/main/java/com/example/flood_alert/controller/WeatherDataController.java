package com.example.flood_alert.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String backfill() {

        weatherDataInitializerService.backfill();

        return "DONE";
    }

    private final StringRedisTemplate stringRedisTemplate;

    @DeleteMapping("/reset-weather")
    public String resetWeather() {
        stringRedisTemplate.delete("weather:backfill_done");
        stringRedisTemplate.delete("weather:last_area_id");
        return "RESET DONE";
    }
    
}
