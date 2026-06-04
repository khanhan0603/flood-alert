package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
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

    static final String FORECAST_URL   = "https://api.open-meteo.com/v1/forecast";
    static final String TIMEZONE       = "Asia/Bangkok";
    static final String HOURLY_FIELDS  = "precipitation,temperature_2m,dew_point_2m,surface_pressure,"
                                       + "wind_speed_10m,wind_direction_10m,relative_humidity_2m,"
                                       + "et0_fao_evapotranspiration";
    static final int    BATCH_SIZE     = 1000; // Open-Meteo tối đa 1000 locations/request
    static final int    KEEP_DAYS      = 8;    // days_back=8 trong Python
    static final int    HOURS_PER_DAY  = 24;
    static final int    BATCH_SLEEP_MS = 2000;

    WeatherDataRepository weatherDataRepository;
    AreaRepository        areaRepository;
    WeatherDataMapper     weatherDataMapper;
    RestTemplateBuilder   restTemplateBuilder;

    // =========================================================================
    // SCHEDULER 1: Backfill — 00:05 mỗi ngày
    // Kiểm tra KEEP_DAYS ngày quá khứ có đủ 24h data không, nếu thiếu thì fetch bù
    // =========================================================================
    @Scheduled(cron = "0 25 19 * * *")
    public void backfill() {
        log.info("=== START BACKFILL CHECK ===");
        List<Area> areas = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        if (areas.isEmpty()) {
            log.info("BACKFILL SKIP: NO AREA FOUND");
            return;
        }

        LocalDate today          = LocalDate.now();
        long      totalAreas     = areas.size();
        Set<LocalDate> incompleteDates = new HashSet<>();

        for (int daysAgo = 1; daysAgo <= KEEP_DAYS; daysAgo++) {
            LocalDate     date  = today.minusDays(daysAgo);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end   = date.plusDays(1).atStartOfDay();

            long completeAreas = weatherDataRepository.countAreasWithFullDay(start, end, HOURS_PER_DAY);

            if (completeAreas < totalAreas) {
                log.info("DATE {} INCOMPLETE: {}/{} areas đủ 24h", date, completeAreas, totalAreas);
                incompleteDates.add(date);
            } else {
                log.info("DATE {} OK: {}/{} areas", date, completeAreas, totalAreas);
            }
        }

        if (incompleteDates.isEmpty()) {
            log.info("=== BACKFILL: ALL {} DAYS COMPLETE ===", KEEP_DAYS);
            return;
        }

        log.info("BACKFILL: {} ngày cần fill: {}", incompleteDates.size(), incompleteDates);
        fetchAndSaveForDates(areas, incompleteDates);
        log.info("=== END BACKFILL ===");
    }

    // =========================================================================
    // SCHEDULER 2: Realtime — đầu mỗi giờ
    // Lấy data current cho hôm nay, 4 request cho 3321 areas
    // =========================================================================
    @Scheduled(cron = "0 20 19 * * *")
    public void fetchRealtime() {
        log.info("=== START REALTIME FETCH ===");
        List<Area> areas = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        if (areas.isEmpty()) {
            log.info("REALTIME SKIP: NO AREA FOUND");
            return;
        }
        fetchAndSaveCurrent(areas);
        log.info("=== END REALTIME FETCH ===");
    }

    // =========================================================================
    // SCHEDULER 3: Cleanup — 23:55 mỗi ngày
    // Xóa data cũ hơn KEEP_DAYS ngày, chỉ giữ đúng 8 ngày + hôm nay
    // =========================================================================
    @Scheduled(cron = "0 55 23 * * *")
    public void deleteOldData() {
        LocalDateTime cutoff = LocalDate.now().minusDays(KEEP_DAYS).atStartOfDay();
        log.info("=== DELETE OLD DATA BEFORE {} ===", cutoff);
        try {
            int deleted = weatherDataRepository.deleteByTimeBefore(cutoff);
            log.info("DELETED {} OLD WEATHER RECORDS", deleted);
        } catch (Exception e) {
            log.error("ERROR DELETING OLD DATA", e);
        }
    }

    // =========================================================================
    // PUBLIC: Trigger thủ công qua API endpoint hoặc lần đầu deploy
    // =========================================================================
    public void fetchManualBackfill() {
        log.info("MANUAL BACKFILL TRIGGERED");
        backfill();
    }

    public void fetchManualRealtime() {
        log.info("MANUAL REALTIME TRIGGERED");
        fetchRealtime();
    }

    // =========================================================================
    // CORE: Fetch & save cho các ngày còn thiếu
    // =========================================================================
    private void fetchAndSaveForDates(List<Area> areas, Set<LocalDate> incompleteDates) {
        RestTemplate restTemplate  = restTemplateBuilder.build();
        int          totalBatches  = (areas.size() + BATCH_SIZE - 1) / BATCH_SIZE;

        // Tính range để query existing times 1 lần/batch
        LocalDateTime rangeStart = incompleteDates.stream()
                .min(LocalDate::compareTo).orElseThrow().atStartOfDay();
        LocalDateTime rangeEnd   = incompleteDates.stream()
                .max(LocalDate::compareTo).orElseThrow().plusDays(1).atStartOfDay();

        for (int i = 0; i < areas.size(); i += BATCH_SIZE) {
            int        batchNo = i / BATCH_SIZE + 1;
            List<Area> batch   = areas.subList(i, Math.min(i + BATCH_SIZE, areas.size()));

            String lats = batch.stream().map(a -> a.getLat().toString()).collect(Collectors.joining(","));
            String lons = batch.stream().map(a -> a.getLon().toString()).collect(Collectors.joining(","));

            String url = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude",     lats)
                    .queryParam("longitude",    lons)
                    .queryParam("past_days",    KEEP_DAYS + 1) // +1 để bao phủ đủ
                    .queryParam("forecast_days", 0)
                    .queryParam("hourly",       HOURLY_FIELDS)
                    .queryParam("timezone",     TIMEZONE)
                    .toUriString();

            log.info("BACKFILL BATCH {}/{} ({} areas)", batchNo, totalBatches, batch.size());

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) {
                    log.warn("NULL RESPONSE BACKFILL BATCH {}", batchNo);
                    continue;
                }

                // Load existing times của cả batch 1 lần → tránh N+1 query
                List<UUID> batchIds = batch.stream().map(Area::getId).collect(Collectors.toList());
                Map<UUID, Set<LocalDateTime>> existingTimesMap = loadExistingTimesMap(
                        batchIds, rangeStart, rangeEnd);

                if (response.isArray()) {
                    for (int j = 0; j < response.size(); j++) {
                        Area             area          = batch.get(j);
                        Set<LocalDateTime> existingTimes = existingTimesMap
                                .getOrDefault(area.getId(), Set.of());
                        saveHourlyForDates(area, response.get(j).path("hourly"),
                                incompleteDates, existingTimes);
                    }
                } else {
                    Area             area          = batch.get(0);
                    Set<LocalDateTime> existingTimes = existingTimesMap
                            .getOrDefault(area.getId(), Set.of());
                    saveHourlyForDates(area, response.path("hourly"),
                            incompleteDates, existingTimes);
                }

                log.info("BACKFILL BATCH {}/{} SUCCESS", batchNo, totalBatches);
                Thread.sleep(BATCH_SLEEP_MS);

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("RATE LIMIT HIT AT BACKFILL BATCH {}/{}", batchNo, totalBatches);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("ERROR BACKFILL BATCH {}/{}", batchNo, totalBatches, e);
            }
        }
    }

    // =========================================================================
    // CORE: Fetch current weather — realtime mỗi giờ
    // =========================================================================
    private void fetchAndSaveCurrent(List<Area> areas) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        int          totalBatches = (areas.size() + BATCH_SIZE - 1) / BATCH_SIZE;

        for (int i = 0; i < areas.size(); i += BATCH_SIZE) {
            int        batchNo = i / BATCH_SIZE + 1;
            List<Area> batch   = areas.subList(i, Math.min(i + BATCH_SIZE, areas.size()));

            String lats = batch.stream().map(a -> a.getLat().toString()).collect(Collectors.joining(","));
            String lons = batch.stream().map(a -> a.getLon().toString()).collect(Collectors.joining(","));

            String url = UriComponentsBuilder
                    .fromUriString(FORECAST_URL)
                    .queryParam("latitude",  lats)
                    .queryParam("longitude", lons)
                    .queryParam("current",   HOURLY_FIELDS)
                    .queryParam("timezone",  TIMEZONE)
                    .toUriString();

            log.info("REALTIME BATCH {}/{} ({} areas)", batchNo, totalBatches, batch.size());

            try {
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) {
                    log.warn("NULL RESPONSE REALTIME BATCH {}", batchNo);
                    continue;
                }

                if (response.isArray()) {
                    for (int j = 0; j < response.size(); j++) {
                        saveCurrentData(batch.get(j), response.get(j).path("current"));
                    }
                } else {
                    saveCurrentData(batch.get(0), response.path("current"));
                }

                log.info("REALTIME BATCH {}/{} SUCCESS", batchNo, totalBatches);
                Thread.sleep(BATCH_SLEEP_MS);

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("RATE LIMIT HIT AT REALTIME BATCH {}/{}", batchNo, totalBatches);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("ERROR REALTIME BATCH {}/{}", batchNo, totalBatches, e);
            }
        }
    }

    // =========================================================================
    // SAVE: Chỉ insert giờ còn thiếu, bỏ qua giờ đã có — O(1) lookup
    // =========================================================================
    private void saveHourlyForDates(Area area, JsonNode hourly,
                                    Set<LocalDate> targetDates,
                                    Set<LocalDateTime> existingTimes) {
        JsonNode times = hourly.path("time");
        if (!times.isArray()) return;

        List<WeatherData> toSave = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            LocalDate     date = time.toLocalDate();

            if (!targetDates.contains(date))   continue; // O(1) — Set
            if (existingTimes.contains(time))  continue; // O(1) — Set

            toSave.add(buildWeatherData(area, hourly, i, time));
        }

        persistBatch(area, toSave);
    }

    // =========================================================================
    // SAVE: Lưu current data — realtime
    // =========================================================================
    private void saveCurrentData(Area area, JsonNode current) {
        if (current.isMissingNode() || current.path("time").isMissingNode()) return;

        LocalDateTime time = LocalDateTime.parse(current.path("time").asText());

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

        WeatherData wd = weatherDataMapper.toWeatherData(request);
        wd.setArea(area);
        wd.setTime(time);

        try {
            weatherDataRepository.save(wd);
        } catch (Exception e) {
            log.debug("DUPLICATE REALTIME AREA {} TIME {}", area.getId(), time);
        }
    }

    // =========================================================================
    // HELPER: Load existing times của nhiều area cùng lúc → Map<areaId, Set<time>>
    // Tránh N+1 query khi xử lý batch
    // =========================================================================
    private Map<UUID, Set<LocalDateTime>> loadExistingTimesMap(
            List<UUID> areaIds, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = weatherDataRepository.findExistingTimesBatch(areaIds, start, end);

        Map<UUID, Set<LocalDateTime>> result=new HashMap<>();

        for(Object[] row:rows){
            UUID areaId=(UUID) row[0];
            LocalDateTime time=(LocalDateTime) row[1];
            result.computeIfAbsent(areaId, k -> new HashSet<>()).add(time);
        }
        return result;
    }

    // =========================================================================
    // HELPER: Build WeatherData entity từ hourly JSON array
    // =========================================================================
    private WeatherData buildWeatherData(Area area, JsonNode hourly, int i, LocalDateTime time) {
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

        WeatherData wd = weatherDataMapper.toWeatherData(request);
        wd.setArea(area);
        wd.setTime(time);
        return wd;
    }

    // =========================================================================
    // HELPER: saveAll với fallback từng record nếu có duplicate
    // =========================================================================
    private void persistBatch(Area area, List<WeatherData> toSave) {
        if (toSave.isEmpty()) {
            log.debug("NO NEW DATA FOR AREA {}", area.getId());
            return;
        }
        try {
            weatherDataRepository.saveAll(toSave);
            log.info("SAVED {} RECORDS FOR AREA {}", toSave.size(), area.getId());
        } catch (Exception e) {
            // Fallback: lưu từng record để bỏ qua các duplicate lẻ
            int saved = 0;
            for (WeatherData wd : toSave) {
                try {
                    weatherDataRepository.save(wd);
                    saved++;
                } catch (Exception ex) {
                    log.debug("DUPLICATE AREA {} TIME {}", area.getId(), wd.getTime());
                }
            }
            log.info("FALLBACK SAVED {}/{} FOR AREA {}", saved, toSave.size(), area.getId());
        }
    }

    // =========================================================================
    // HELPER: Parse BigDecimal an toàn từ JsonNode
    // =========================================================================
    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) return null;
        return BigDecimal.valueOf(value.asDouble());
    }

    private BigDecimal decimal(JsonNode node, String field, int index) {
        JsonNode values = node.path(field);
        if (!values.isArray() || index >= values.size() || values.get(index).isNull()) return null;
        return BigDecimal.valueOf(values.get(index).asDouble());
    }
}