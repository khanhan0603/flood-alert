package com.example.flood_alert.service;

import java.math.BigDecimal;
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
    static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    static final String TIMEZONE = "Asia/Bangkok";
    static final String HOURLY_FIELDS = "precipitation,temperature_2m,dew_point_2m,surface_pressure,wind_speed_10m,wind_direction_10m,relative_humidity_2m,et0_fao_evapotranspiration";
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
        weatherDataService.checkCompleted();
        if (!weatherDataService.isSchedulerEnabled()) {
            return;
        }
        Pageable pageable = PageRequest.of(0, 100);
        List<Area> areas = areaRepository.findAreasWithoutWeather(pageable);
        if (areas.isEmpty()) {
            log.info("NO AREA NEED IMPORT");
            return;
        }
        fetchAndSaveArchive(areas);
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
            String url = UriComponentsBuilder.fromUriString(FORECAST_URL).queryParam("latitude", area.getLat())
                    .queryParam("longitude", area.getLon()).queryParam("current", HOURLY_FIELDS)
                    .queryParam("timezone", TIMEZONE).toUriString();
            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null)
                    continue;
                saveHourlyData(area, response.path("current"));
            } catch (Exception e) {
                log.error("ERROR FETCH CURRENT WEATHER FOR AREA {}", area.getId(), e);
            }
        }
    }

    private void fetchAndSaveArchive(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        for (Area area : areas) {
            String url = UriComponentsBuilder.fromUriString(FORECAST_URL).queryParam("latitude", area.getLat())
                    .queryParam("longitude", area.getLon()).queryParam("past_days", 31).queryParam("forecast_days", 0)
                    .queryParam("hourly", HOURLY_FIELDS).queryParam("timezone", TIMEZONE).toUriString();
            try {
                log.info("FETCH WEATHER FOR AREA {}", area.getId());
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) {
                    continue;
                }
                saveHourlyData(area, response.path("hourly"));
                log.info("SUCCESS AREA {}", area.getId());
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
        if (!times.isArray()) {
            return;
        }
        List<WeatherData> weatherDatas = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            WeatherDataCreationRequest request = WeatherDataCreationRequest.builder()
                    .precipitation(decimal(hourly, "precipitation", i))
                    .temperature2m(decimal(hourly, "temperature_2m", i)).dewpoint2m(decimal(hourly, "dew_point_2m", i))
                    .surfacePressure(decimal(hourly, "surface_pressure", i))
                    .windspeed10m(decimal(hourly, "wind_speed_10m", i))
                    .winddirection10m(decimal(hourly, "wind_direction_10m", i))
                    .relativehumidity2m(decimal(hourly, "relative_humidity_2m", i))
                    .evapotranspiration(decimal(hourly, "et0_fao_evapotranspiration", i)).lat(area.getLat())
                    .lon(area.getLon()).build();
            WeatherData weatherData = weatherDataMapper.toWeatherData(request);
            weatherData.setArea(area);
            weatherData.setTime(time);
            weatherDatas.add(weatherData);
        }
        weatherDataRepository.saveAll(weatherDatas);
    }

    private BigDecimal decimal(JsonNode node, String field, int index) {
        JsonNode values = node.path(field);
        if (!values.isArray() || index >= values.size() || values.get(index).isNull()) {
            return null;
        }
        return BigDecimal.valueOf(values.get(index).asDouble());
    }
}