package com.example.flood_alert.service;

import org.springframework.stereotype.Service;

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

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void checkCompleted() {

        long importedAreaCount =
            weatherDataRepository.countDistinctAreaId();

        log.info(
            "WEATHER IMPORTED AREA: {}",
            importedAreaCount
        );

        if (importedAreaCount >= TOTAL_AREA) {

            schedulerEnabled = false;

            log.info(
                "IMPORT COMPLETED -> DISABLE SCHEDULER"
            );
        }
    }
}