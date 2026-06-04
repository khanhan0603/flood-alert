package com.example.flood_alert.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.AreaWeatherResponse;
import com.example.flood_alert.dbo.response.WDataResponse;
import com.example.flood_alert.mapper.WeatherDataMapper;
import com.example.flood_alert.repository.WeatherDataRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherDataService {

    static final long TOTAL_AREA = 3321;

    final WeatherDataRepository weatherDataRepository;

    boolean schedulerEnabled = true;

    WeatherDataMapper weatherDataMapper;

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public List<AreaWeatherResponse> findDistinctAreaIdAndTenKhuvuc() {
        return weatherDataRepository.findDistinctAreaIdAndTenKhuvuc();
    }

    public List<WDataResponse> findByAreaId(UUID area_id) {
        return weatherDataRepository.findWeatherResponseByAreaId(area_id);
    }

    public List<WDataResponse> findByAreaIdAndTime(UUID areaId, LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        return weatherDataRepository
                .findWeatherDataByAreaAndTime(areaId, startTime, endTime)
                .stream()
                .map(weatherDataMapper::toWDataResponse)
                .toList();
    }
}