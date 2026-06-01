package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.flood_alert.dbo.request.WeatherDataCreationRequest;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.WeatherData;
import com.example.flood_alert.mapper.WeatherDataMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.WeatherDataRepository;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeatherDataInitializerService {

    static final String FORECAST_URL =
        "https://api.open-meteo.com/v1/forecast";

    static final String ARCHIVE_URL =
        "https://archive-api.open-meteo.com/v1/archive";

    static final String TIMEZONE =
        "Asia/Bangkok";

    static final String HOURLY_FIELDS =
        "precipitation,temperature_2m,dew_point_2m,surface_pressure,wind_speed_10m,wind_direction_10m,relative_humidity_2m,et0_fao_evapotranspiration";

    WeatherDataRepository weatherDataRepository;
    AreaRepository areaRepository;
    WeatherDataMapper weatherDataMapper;
    RestTemplateBuilder restTemplateBuilder;
    WeatherDataService weatherDataService;

    @Scheduled(cron = "0 */5 * * * *")
    public void backfill() {
        if (!weatherDataService.isSchedulerEnabled()) {
            log.info("SCHEDULER DISABLED");
            return;
        }

        if (!weatherDataService.isSchedulerEnabled()) {
            return;
        }

        Pageable pageable = PageRequest.of(0, 100);
        log.info("BACKFILL START");

        List<Area> areas = areaRepository.findAreasWithoutWeather(pageable);
        log.info("AREAS WITHOUT WEATHER = {}", areas.size());

        if (!areas.isEmpty()) {
            fetchAndSaveArchive(areas);
            return;
        }

        List<Area> allArea = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        fetchMissingWeather(allArea);
    }

    @Scheduled(cron = "0 5 * * * *")
    public void fetchHourlyScheduler() {
        fetchHourly();
    }

    public void fetchHourly() {
        List<Area> areas = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        if (areas.isEmpty()) {
            log.info("SKIP HOURLY WEATHER FETCH: NO AREA");
            return;
        }
        fetchAndSaveCurrent(areas);
    }

    private void fetchAndSaveCurrent(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        for (Area area : areas) {
            String url = UriComponentsBuilder
                .fromUriString(FORECAST_URL)
                .queryParam("latitude", area.getLat())
                .queryParam("longitude", area.getLon())
                .queryParam("current", HOURLY_FIELDS)
                .queryParam("timezone", TIMEZONE)
                .toUriString();

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) continue;
                saveCurrentData(area, response.path("current"));
            } catch (Exception e) {
                log.error("ERROR FETCH CURRENT WEATHER FOR AREA {}", area.getId(), e);
            }
        }
    }

    private void saveCurrentData(Area area, JsonNode current) {
        if (current.isMissingNode() || current.path("time").isMissingNode()) {
            return;
        }
        WeatherDataCreationRequest request = WeatherDataCreationRequest.builder()
            .precipitation(decimal(current, "precipitation"))
            .temperature2m(decimal(current, "temperature_2m"))
            .dewpoint2m(decimal(current, "dew_point_2m"))
            .surfacePressure(decimal(current, "surface_pressure"))
            .windspeed10m(decimal(current, "wind_speed_10m"))
            .winddirection10m(decimal(current, "wind_direction_10m"))
            .relativehumidity2m(decimal(current, "relative_humidity_2m"))
            .evapotranspiration(decimal(current, "et0_fao_evapotranspiration"))
            .lat(area.getLat())
            .lon(area.getLon())
            .build();

        WeatherData weatherData = weatherDataMapper.toWeatherData(request);
        weatherData.setArea(area);
        weatherData.setTime(LocalDateTime.parse(current.path("time").asText()));

        try {
            weatherDataRepository.save(weatherData);
        } catch (Exception e) {
            log.debug("DUPLICATE CURRENT WEATHER AREA {} TIME {}", area.getId(), weatherData.getTime());
        }
    }

    private void fetchAndSaveArchive(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        for (Area area : areas) {
            String url = UriComponentsBuilder
                .fromUriString(FORECAST_URL)
                .queryParam("latitude", area.getLat())
                .queryParam("longitude", area.getLon())
                .queryParam("past_days", 31)
                .queryParam("forecast_days", 0)
                .queryParam("hourly", HOURLY_FIELDS)
                .queryParam("timezone", TIMEZONE)
                .toUriString();

            try {
                log.info("FETCH WEATHER FOR AREA {}", area.getId());
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) continue;

                saveHourlyData(area, response.path("hourly"), null);
                log.info("SUCCESS AREA {}", area.getId());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH ARCHIVE WEATHER FOR AREA {}", area.getId(), e);
            }
        }
    }

    public void fetchMissingWeather(List<Area> areas) {
        log.info("ENTER FETCH MISSING WEATHER, AREA SIZE={}", areas.size());
        RestTemplate restTemplate = restTemplateBuilder.build();

        for (Area area : areas) {
            try {
                LocalDateTime lastTime = weatherDataRepository.findMaxTimeByAreaId(area.getId());
                if (lastTime == null) continue;

                LocalDate startDate = lastTime.toLocalDate();
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                // ✅ Bước 1: Fetch archive cho data từ startDate đến hôm qua
                if (!startDate.isAfter(yesterday)) {
                    String archiveUrl = UriComponentsBuilder
                        .fromUriString(ARCHIVE_URL)
                        .queryParam("latitude", area.getLat())
                        .queryParam("longitude", area.getLon())
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", yesterday)
                        .queryParam("hourly", HOURLY_FIELDS)
                        .queryParam("timezone", TIMEZONE)
                        .toUriString();

                    log.info(
                        "FETCH ARCHIVE MISSING WEATHER AREA {} FROM {} TO {}",
                        area.getId(), startDate, yesterday
                    );

                    JsonNode archiveResponse = restTemplate.getForObject(archiveUrl, JsonNode.class);
                    if (archiveResponse != null) {
                        saveHourlyData(area, archiveResponse.path("hourly"), lastTime);
                    }
                    Thread.sleep(500);
                }

                // ✅ Bước 2: Fetch forecast cho hôm nay (archive không có hôm nay)
                String forecastUrl = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude", area.getLat())
                    .queryParam("longitude", area.getLon())
                    .queryParam("start_date", today)
                    .queryParam("end_date", today)
                    .queryParam("hourly", HOURLY_FIELDS)
                    .queryParam("timezone", TIMEZONE)
                    .toUriString();

                log.info("FETCH FORECAST TODAY AREA {} DATE {}", area.getId(), today);

                JsonNode forecastResponse = restTemplate.getForObject(forecastUrl, JsonNode.class);
                if (forecastResponse != null) {
                    // lastTime được cập nhật lại sau khi đã save archive
                    LocalDateTime updatedLastTime = weatherDataRepository.findMaxTimeByAreaId(area.getId());
                    saveHourlyData(area, forecastResponse.path("hourly"), updatedLastTime);
                }

                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH MISSING WEATHER AREA {}", area.getId(), e);
            }
        }
    }

    private void saveHourlyData(Area area, JsonNode hourly, LocalDateTime lastTime) {
        JsonNode times = hourly.path("time");
        if (!times.isArray()) return;

        List<WeatherData> weatherDatas = new ArrayList<>();

        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());

            if (lastTime != null && !time.isAfter(lastTime)) {
                continue;
            }

            WeatherDataCreationRequest request = WeatherDataCreationRequest.builder()
                .precipitation(decimal(hourly, "precipitation", i))
                .temperature2m(decimal(hourly, "temperature_2m", i))
                .dewpoint2m(decimal(hourly, "dew_point_2m", i))
                .surfacePressure(decimal(hourly, "surface_pressure", i))
                .windspeed10m(decimal(hourly, "wind_speed_10m", i))
                .winddirection10m(decimal(hourly, "wind_direction_10m", i))
                .relativehumidity2m(decimal(hourly, "relative_humidity_2m", i))
                .evapotranspiration(decimal(hourly, "et0_fao_evapotranspiration", i))
                .lat(area.getLat())
                .lon(area.getLon())
                .build();

            WeatherData weatherData = weatherDataMapper.toWeatherData(request);
            weatherData.setArea(area);
            weatherData.setTime(time);
            weatherDatas.add(weatherData);
        }

        if (!weatherDatas.isEmpty()) {
            weatherDataRepository.saveAll(weatherDatas);
            log.info("SAVED {} RECORDS FOR AREA {}", weatherDatas.size(), area.getId());
        }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull()) return null;
        return BigDecimal.valueOf(value.asDouble());
    }

    private BigDecimal decimal(JsonNode node, String field, int index) {
        JsonNode values = node.path(field);
        if (!values.isArray() || index >= values.size() || values.get(index).isNull()) {
            return null;
        }
        return BigDecimal.valueOf(values.get(index).asDouble());
    }
}