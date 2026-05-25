package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.flood_alert.dbo.response.AreaWeatherResponse;
import com.example.flood_alert.dbo.response.WDataResponse;
import com.example.flood_alert.entity.WeatherData;
import com.example.flood_alert.service.WeatherDataInitializerService;
import com.example.flood_alert.service.WeatherDataService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/weather-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeatherDataController {

    WeatherDataInitializerService weatherDataInitializerService;
    WeatherDataService weatherDataService;
    //StringRedisTemplate stringRedisTemplate;

    @PostMapping("/backfill")
    public String backfill() {
        weatherDataInitializerService.backfill();
        return "DONE";
    }

    // @DeleteMapping("/reset-weather")
    // public String resetWeather() {
    //     stringRedisTemplate.delete("weather:backfill_done");
    //     stringRedisTemplate.delete("weather:last_area_id");

    //     String backfillDone = stringRedisTemplate.opsForValue().get("weather:backfill_done");
    //     String lastAreaId = stringRedisTemplate.opsForValue().get("weather:last_area_id");

    //     return "backfill_done=" + backfillDone + ", last_area_id=" + lastAreaId;
    // }

    @GetMapping("/count-area")
    public String countArea() {
        weatherDataService.checkCompleted();
        return "CHECK COMPLETED";
    }

    @GetMapping("/list-area")
    public List<AreaWeatherResponse> findDistinctAreaIdAndTenKhuvuc() {
        return weatherDataService.findDistinctAreaIdAndTenKhuvuc();
    }
    

    @GetMapping("/find-by-area-id")
    public List<WDataResponse> findByAreaId(@RequestParam UUID area_id) {
        return weatherDataService.findByAreaId(area_id);
    }
}