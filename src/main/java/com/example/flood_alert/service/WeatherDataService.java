package com.example.flood_alert.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.AreaWeatherResponse;
import com.example.flood_alert.dbo.response.WDataResponse;
import com.example.flood_alert.entity.WeatherData;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.WeatherDataMapper;
import com.example.flood_alert.repository.WeatherDataRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeatherDataService {

    // long TOTAL_AREA = 3321;

    WeatherDataRepository weatherDataRepository;

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

    public List<WDataResponse> filterWeatherData(
            UUID areaId,
            LocalDate start,
            LocalDate end) {

        // Không có điều kiện lọc
        if (areaId == null && start == null && end == null) {
            throw new AppException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // Chỉ nhập một trong hai ngày
        if ((start == null) != (end == null)) {
            throw new AppException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // To < From
        if (start != null && end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        List<WeatherData> weatherData;

        // Không lọc
        if (areaId == null && start == null) {

            weatherData = weatherDataRepository
                    .findAllByOrderByTimeAsc();

        }
        // Chỉ lọc khu vực
        else if (areaId != null && start == null) {

            weatherData = weatherDataRepository
                    .findByArea_IdOrderByTimeAsc(areaId);

        }
        // Chỉ lọc thời gian
        else if (areaId == null) {

            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.plusDays(1).atStartOfDay();

            weatherData = weatherDataRepository
                    .findByTimeGreaterThanEqualAndTimeLessThanOrderByTimeAsc(
                            startTime,
                            endTime);

        }
        // Lọc khu vực + thời gian
        else {

            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.plusDays(1).atStartOfDay();

            weatherData = weatherDataRepository
                    .findByArea_IdAndTimeGreaterThanEqualAndTimeLessThanOrderByTimeAsc(
                            areaId,
                            startTime,
                            endTime);
        }

        return weatherData.stream()
                .map(weatherDataMapper::toWDataResponse)
                .toList();
    }
}