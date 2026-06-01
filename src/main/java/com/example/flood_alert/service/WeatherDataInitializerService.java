package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    static final String TIMEZONE = "Asia/Bangkok";
    static final String HOURLY_FIELDS = "precipitation,temperature_2m,dew_point_2m,surface_pressure,wind_speed_10m,wind_direction_10m,relative_humidity_2m,et0_fao_evapotranspiration";
    static final int BATCH_SIZE = 10;
    static final long RATE_LIMIT_COOLDOWN_MS = 60 * 60 * 1000L; // chờ 1 tiếng trước khi thử lại

    WeatherDataRepository weatherDataRepository;
    AreaRepository areaRepository;
    WeatherDataMapper weatherDataMapper;
    RestTemplateBuilder restTemplateBuilder;
    WeatherDataService weatherDataService;

    // ✅ Không dùng @FieldDefaults makeFinal nên khai báo thủ công
    private final java.util.concurrent.atomic.AtomicLong rateLimitUntil = new java.util.concurrent.atomic.AtomicLong(0);

    @Scheduled(cron = "0 */5 * * * *")
    public void backfill() {
        if (!weatherDataService.isSchedulerEnabled()) {
            log.info("SCHEDULER DISABLED");
            return;
        }

        // ✅ Kiểm tra đang trong cooldown rate limit không
        if (System.currentTimeMillis() < rateLimitUntil.get()) {
            log.info("RATE LIMIT COOLDOWN, SKIP BACKFILL UNTIL {}",
                    java.time.Instant.ofEpochMilli(rateLimitUntil.get()));
            return;
        }

        Pageable pageable = PageRequest.of(0, 500);

        List<Area> areasWithoutWeather = areaRepository.findAreasWithoutWeather(pageable);
        log.info("AREAS WITHOUT WEATHER = {}", areasWithoutWeather.size());

        if (!areasWithoutWeather.isEmpty()) {
            fetchAndSaveArchive(areasWithoutWeather);
            return;
        }

        LocalDate today = LocalDate.now();
        for (int daysAgo = 1; daysAgo <= 31; daysAgo++) {
            LocalDate missingDate = today.minusDays(daysAgo);
            LocalDateTime startDate = missingDate.atStartOfDay();
            LocalDateTime endDate = missingDate.plusDays(1).atStartOfDay();

            List<Area> missingAreas = areaRepository.findAreasMissingDataInRange(startDate, endDate, pageable);

            if (!missingAreas.isEmpty()) {
                log.info("AREAS MISSING DATA ON {} = {}", missingDate, missingAreas.size());
                fetchMissingWeatherForDate(missingAreas, missingDate);
                return;
            }
        }

        log.info("ALL DATA COMPLETE FOR LAST 31 DAYS");
    }

    private void fetchMissingWeatherForDate(List<Area> areas, LocalDate missingDate) {
        log.info("FETCH MISSING DATE {}, SIZE={}", missingDate, areas.size());
        RestTemplate restTemplate = restTemplateBuilder.build();

        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(missingDate, LocalDate.now());
        int pastDays = (int) Math.min(daysAgo + 1, 92);

        for (int i = 0; i < areas.size(); i += BATCH_SIZE) {
            logMemory();
            List<Area> batch = areas.subList(i, Math.min(i + BATCH_SIZE, areas.size()));

            String lats = batch.stream().map(a -> a.getLat().toString()).collect(Collectors.joining(","));
            String lons = batch.stream().map(a -> a.getLon().toString()).collect(Collectors.joining(","));

            String url = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude", lats)
                    .queryParam("longitude", lons)
                    .queryParam("past_days", pastDays)
                    .queryParam("forecast_days", 0)
                    .queryParam("hourly", HOURLY_FIELDS)
                    .queryParam("timezone", TIMEZONE)
                    .toUriString();

            log.info("FETCH MISSING DATE {} BATCH {}/{}",
                    missingDate, i / BATCH_SIZE + 1, (areas.size() + BATCH_SIZE - 1) / BATCH_SIZE);

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null)
                    continue;

                if (response.isArray()) {
                    for (int j = 0; j < response.size(); j++) {
                        saveHourlyDataForDate(batch.get(j), response.get(j).path("hourly"), missingDate);
                    }
                } else {
                    saveHourlyDataForDate(batch.get(0), response.path("hourly"), missingDate);
                }

                Thread.sleep(1000);

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("429 BODY = {}", e.getResponseBodyAsString());

                rateLimitUntil.set(System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS);

                log.warn("RATE LIMIT HIT, COOLDOWN 1 HOUR");
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH MISSING DATE {} BATCH {}", missingDate, i / BATCH_SIZE + 1, e);
            }
        }
    }

    @Scheduled(cron = "0 5 * * * *")
    public void fetchHourlyScheduler() {
        fetchHourly();
    }

    public void fetchHourly() {
        if (System.currentTimeMillis() < rateLimitUntil.get()) {
            log.info("RATE LIMIT COOLDOWN, SKIP HOURLY FETCH");
            return;
        }
        List<Area> areas = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        if (areas.isEmpty()) {
            log.info("SKIP HOURLY WEATHER FETCH: NO AREA");
            return;
        }
        fetchAndSaveCurrent(areas);
    }

    private void fetchAndSaveCurrent(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        for (int i = 0; i < areas.size(); i += BATCH_SIZE) {
            logMemory();
            List<Area> batch = areas.subList(i, Math.min(i + BATCH_SIZE, areas.size()));

            String lats = batch.stream().map(a -> a.getLat().toString()).collect(Collectors.joining(","));
            String lons = batch.stream().map(a -> a.getLon().toString()).collect(Collectors.joining(","));

            String url = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude", lats)
                    .queryParam("longitude", lons)
                    .queryParam("current", HOURLY_FIELDS)
                    .queryParam("timezone", TIMEZONE)
                    .toUriString();

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null)
                    continue;

                if (response.isArray()) {
                    for (int j = 0; j < response.size(); j++) {
                        saveCurrentData(batch.get(j), response.get(j).path("current"));
                    }
                } else {
                    saveCurrentData(batch.get(0), response.path("current"));
                }

                Thread.sleep(200);

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("429 BODY = {}", e.getResponseBodyAsString());

                rateLimitUntil.set(System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS);

                log.warn("RATE LIMIT HIT, COOLDOWN 1 HOUR");
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH CURRENT BATCH {}", i / BATCH_SIZE + 1, e);
            }
        }
    }

    private void saveCurrentData(Area area, JsonNode current) {
        if (current.isMissingNode() || current.path("time").isMissingNode())
            return;

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
            log.debug("DUPLICATE CURRENT AREA {} TIME {}", area.getId(), weatherData.getTime());
        }
    }

    private void fetchAndSaveArchive(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        for (int i = 0; i < areas.size(); i += BATCH_SIZE) {
            logMemory();
            List<Area> batch = areas.subList(i, Math.min(i + BATCH_SIZE, areas.size()));

            String lats = batch.stream().map(a -> a.getLat().toString()).collect(Collectors.joining(","));
            String lons = batch.stream().map(a -> a.getLon().toString()).collect(Collectors.joining(","));

            String url = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude", lats)
                    .queryParam("longitude", lons)
                    .queryParam("past_days", 31)
                    .queryParam("forecast_days", 0)
                    .queryParam("hourly", HOURLY_FIELDS)
                    .queryParam("timezone", TIMEZONE)
                    .toUriString();

            try {
                log.info("FETCH ARCHIVE BATCH {}/{}",
                        i / BATCH_SIZE + 1, (areas.size() + BATCH_SIZE - 1) / BATCH_SIZE);

                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null)
                    continue;

                if (response.isArray()) {
                    for (int j = 0; j < response.size(); j++) {
                        Area area = batch.get(j);
                        LocalDateTime lastTime = weatherDataRepository.findMaxTimeByAreaId(area.getId());
                        saveHourlyData(area, response.get(j).path("hourly"), lastTime);
                    }
                } else {
                    Area area = batch.get(0);
                    LocalDateTime lastTime = weatherDataRepository.findMaxTimeByAreaId(area.getId());
                    saveHourlyData(area, response.path("hourly"), lastTime);
                }

                log.info("SUCCESS ARCHIVE BATCH {}", i / BATCH_SIZE + 1);
                Thread.sleep(1500);

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("429 BODY = {}", e.getResponseBodyAsString());

                rateLimitUntil.set(System.currentTimeMillis() + RATE_LIMIT_COOLDOWN_MS);

                log.warn("RATE LIMIT HIT, COOLDOWN 1 HOUR");
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH ARCHIVE BATCH {}", i / BATCH_SIZE + 1, e);
            }
        }
    }

    private void saveHourlyDataForDate(Area area, JsonNode hourly, LocalDate targetDate) {
        JsonNode times = hourly.path("time");
        if (!times.isArray())
            return;

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        List<WeatherData> weatherDatas = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            if (time.isBefore(startOfDay) || !time.isBefore(endOfDay))
                continue;

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
            try {
                int count = weatherDatas.size();
                weatherDataRepository.saveAll(weatherDatas);
                weatherDatas.clear();
                log.info("SAVED {} RECORDS FOR AREA {} DATE {}", count, area.getId(), targetDate);
            } catch (Exception e) {
                for (WeatherData wd : weatherDatas) {
                    try {
                        weatherDataRepository.save(wd);
                    } catch (Exception ex) {
                        log.debug("DUPLICATE AREA {} TIME {}", area.getId(), wd.getTime());
                    }
                }
            }
        }
    }

    private void saveHourlyData(Area area, JsonNode hourly, LocalDateTime lastTime) {
        JsonNode times = hourly.path("time");
        if (!times.isArray())
            return;

        List<WeatherData> weatherDatas = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            if (lastTime != null && !time.isAfter(lastTime))
                continue;

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
            int count = weatherDatas.size();
            weatherDataRepository.saveAll(weatherDatas);
            weatherDatas.clear();
            log.info("SAVED {} RECORDS FOR AREA {}", count, area.getId());
        }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode())
            return null;
        return BigDecimal.valueOf(value.asDouble());
    }

    private BigDecimal decimal(JsonNode node, String field, int index) {
        JsonNode values = node.path(field);
        if (!values.isArray() || index >= values.size() || values.get(index).isNull())
            return null;
        return BigDecimal.valueOf(values.get(index).asDouble());
    }

    private void logMemory() {
        long used = (Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory())
                / 1024 / 1024;

        long max = Runtime.getRuntime().maxMemory()
                / 1024 / 1024;

        log.info("MEMORY USED={}MB / MAX={}MB", used, max);
    }
}