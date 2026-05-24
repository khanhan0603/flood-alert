package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    static final String TIMEZONE =
        "Asia/Bangkok";

    static final String HOURLY_FIELDS =
        "precipitation,temperature_2m,dew_point_2m,surface_pressure,wind_speed_10m,wind_direction_10m,relative_humidity_2m,et0_fao_evapotranspiration";

    static final String LAST_AREA_ID_KEY =
        "weather:last_area_id";

    static final String BACKFILL_DONE_KEY =
        "weather:backfill_done";

    StringRedisTemplate stringRedisTemplate;

    WeatherDataRepository weatherDataRepository;

    AreaRepository areaRepository;

    WeatherDataMapper weatherDataMapper;

    RestTemplateBuilder restTemplateBuilder;

    @Scheduled(cron = "0 */5 * * * *")
    public void backfill() {

        String completed =
            stringRedisTemplate
                .opsForValue()
                .get(BACKFILL_DONE_KEY);

        if ("true".equals(completed)) {
            log.info("BACKFILL ALREADY COMPLETED");
            return;
        }

        List<Area> areas = getAreasWithLocation();

        if (areas.isEmpty()) {
            stringRedisTemplate
                .opsForValue()
                .set(BACKFILL_DONE_KEY, "true");
            log.info("ALL AREAS PROCESSED");
            return;
        }

        fetchAndSaveArchive(areas);

        log.info("BACKFILL DONE FOR {} AREAS", areas.size());
    }

    public void fetchHourly() {

        List<Area> areas = getAreasWithLocation();

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
                saveHourlyData(area, response.path("current"));
            } catch (Exception e) {
                log.error("ERROR FETCH CURRENT WEATHER FOR AREA {}", area.getId(), e);
            }
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

            log.info("URL = {}", url);

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) continue;
                saveHourlyData(area, response.path("hourly"));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("ERROR FETCH ARCHIVE WEATHER FOR AREA {}", area.getId(), e);
            }
        }
    }

    private void saveHourlyData(Area area, JsonNode hourly) {

        JsonNode times = hourly.path("time");
        if (!times.isArray()) return;

        List<WeatherData> weatherDatas = new ArrayList<>();

        for (int i = 0; i < times.size(); i++) {

            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());

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

        weatherDataRepository.saveAll(weatherDatas);
    }

    private List<Area> getAreasWithLocation() {

        UUID lastId = getLastId();

        List<Area> areas = areaRepository
            .findTop100ByLevelAndIdGreaterThanAndLatIsNotNullAndLonIsNotNullOrderById(2, lastId);

        if (areas.isEmpty()) {
            return List.of();
        }

        // Skip area đã có weather data
        List<UUID> areaIds = areas.stream()
            .map(Area::getId)
            .toList();

        List<UUID> existingAreaIds = weatherDataRepository
            .findDistinctAreaIdsByAreaIdIn(areaIds);

        List<Area> filteredAreas = areas.stream()
            .filter(a -> !existingAreaIds.contains(a.getId()))
            .toList();

        UUID newLastId = areas.get(areas.size() - 1).getId();
        saveLastId(newLastId);

        log.info(
            "FETCHED AREAS UP TO ID: {}, SKIP {} ALREADY EXIST, FETCH {}",
            newLastId,
            areas.size() - filteredAreas.size(),
            filteredAreas.size()
        );

        return filteredAreas;
    }

    private UUID getLastId() {

        String value = stringRedisTemplate
            .opsForValue()
            .get(LAST_AREA_ID_KEY);

        if (value == null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }

        return UUID.fromString(value);
    }

    private void saveLastId(UUID id) {

        stringRedisTemplate
            .opsForValue()
            .set(LAST_AREA_ID_KEY, id.toString());
    }

    private BigDecimal decimal(JsonNode node, String field, int index) {

        JsonNode values = node.path(field);

        if (!values.isArray() || index >= values.size() || values.get(index).isNull()) {
            return null;
        }

        return BigDecimal.valueOf(values.get(index).asDouble());
    }
}