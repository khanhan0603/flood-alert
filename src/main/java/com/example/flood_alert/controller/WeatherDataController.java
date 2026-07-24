package com.example.flood_alert.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.AreaWeatherResponse;
import com.example.flood_alert.dbo.response.WDataResponse;
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

    // @PostMapping("/backfill")
    // public String backfill() {
    // weatherDataInitializerService.backfill();
    // return "DONE";
    // }

    @GetMapping("/list-area")
    public List<AreaWeatherResponse> findDistinctAreaIdAndTenKhuvuc() {
        return weatherDataService.findDistinctAreaIdAndTenKhuvuc();
    }

    @GetMapping("/find-by-area-id")
    public List<WDataResponse> findByAreaId(@RequestParam UUID area_id) {
        return weatherDataService.findByAreaId(area_id);
    }

    @GetMapping("/find-area-id-time")
    public List<WDataResponse> findByAreaIdAndTime(
            @RequestParam UUID areaId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        return weatherDataService.findByAreaIdAndTime(areaId, start, end);
    }

    // test trước khi 00:30 fill dữ liệu
    // @PostMapping("/sync-now")
    // public String syncNow() {
    // weatherDataInitializerService.triggerManualSync();
    // return "OK";
    // }
    @PostMapping("/admin/weather/{areaId}/refill")
    public ResponseEntity<Void> refillWeather(@PathVariable UUID areaId) {

        weatherDataInitializerService.refillWeather(areaId);

        return ResponseEntity.ok().build();
    }
}