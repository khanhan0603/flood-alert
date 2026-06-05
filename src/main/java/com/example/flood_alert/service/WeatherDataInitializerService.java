package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
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

    static final String FORECAST_URL  = "https://api.open-meteo.com/v1/forecast";
    static final String TIMEZONE      = "Asia/Bangkok";
    static final String HOURLY_FIELDS =
            "precipitation,temperature_2m,dew_point_2m,surface_pressure,"
            + "wind_speed_10m,wind_direction_10m,relative_humidity_2m,"
            + "et0_fao_evapotranspiration";

    /** Số ngày quá khứ lưu trong DB (phục vụ AI + biểu đồ 8 ngày) */
    static final int PAST_DAYS     = 8;
    /** Số ngày forecast (phục vụ AI dự báo hôm nay + 2 ngày tới) */
    static final int FORECAST_DAYS = 3;

    /**
     * Delay giữa mỗi request.
     * 1000ms × 3321 area ≈ 55 phút/lần sync — an toàn với free tier.
     * Có thể giảm xuống 500ms sau khi xác nhận không bị 429.
     */
    static final int REQUEST_DELAY_MS = 1_000;

    static final int RETRY_ATTEMPTS   = 5;
    static final int RETRY_BACKOFF_MS = 5_000;

    WeatherDataRepository weatherDataRepository;
    AreaRepository        areaRepository;
    WeatherDataMapper     weatherDataMapper;
    RestTemplateBuilder   restTemplateBuilder;

    // =========================================================================
    // SCHEDULER 1: 00:30 VN — sau model 17h UTC hôm qua
    // =========================================================================
    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void syncMidnight() {
        syncAll("SYNC-00h30");
    }

    // =========================================================================
    // SCHEDULER 2: 12:30 VN — sau model 05h UTC hôm nay
    // =========================================================================
    @Scheduled(cron = "0 30 12 * * *", zone = "Asia/Ho_Chi_Minh")
    public void syncNoon() {
        syncAll("SYNC-12h30");
    }

    // =========================================================================
    // SCHEDULER 3: Cleanup — 23:55 mỗi ngày, xoá data cũ hơn PAST_DAYS ngày
    // =========================================================================
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deleteOldData() {
        // Xoá data quá khứ cũ hơn PAST_DAYS ngày.
        // Forecast (time > now) giữ nguyên cho AI.
        LocalDateTime cutoff = LocalDate.now().minusDays(PAST_DAYS).atStartOfDay();
        log.info("=== DELETE OLD DATA BEFORE {} ===", cutoff);
        try {
            int deleted = weatherDataRepository.deleteByTimeBefore(cutoff);
            log.info("DELETED {} OLD WEATHER RECORDS", deleted);
        } catch (Exception e) {
            log.error("ERROR DELETING OLD DATA", e);
        }
    }

    // =========================================================================
    // PUBLIC: Trigger thủ công (dùng cho admin API hoặc test)
    // =========================================================================
    public void triggerManualSync() {
        log.info("MANUAL SYNC TRIGGERED");
        syncAll("MANUAL");
    }

    /**
     * On-demand: Controller gọi method này khi user mở 1 phường/xã
     * mà last_synced_at của area đó đã cũ hơn ngưỡng cho phép.
     * Chỉ tốn 1 request, không ảnh hưởng batch.
     */
    public boolean fetchOnDemand(Area area) {
        log.info("ON-DEMAND FETCH AREA {}", area.getId());
        RestTemplate restTemplate = buildRestTemplate();
        try {
            JsonNode response = fetchJsonWithRetry(
                    restTemplate, buildForecastUrl(area), "ON-DEMAND", area.getId());
            if (response == null) return false;
            insertIgnoreDuplicate(area, response.path("hourly"));
            log.info("ON-DEMAND SUCCESS AREA {}", area.getId());
            return true;
        } catch (Exception e) {
            log.error("ON-DEMAND ERROR AREA {}", area.getId(), e);
            return false;
        }
    }

    // =========================================================================
    // CORE: Duyệt toàn bộ area, fetch + upsert
    // =========================================================================
    private void syncAll(String label) {
        log.info("=== START [{}] ===", label);

        List<Area> areas = areaRepository.findByLevelAndLatIsNotNullAndLonIsNotNull(2);
        if (areas.isEmpty()) {
            log.info("[{}] SKIP: no area found", label);
            return;
        }

        RestTemplate restTemplate = buildRestTemplate();
        int total   = areas.size();
        int success = 0;
        int failed  = 0;

        for (int i = 0; i < total; i++) {
            Area area = areas.get(i);
            log.info("[{}] {}/{} area={}", label, i + 1, total, area.getId());

            try {
                JsonNode response = fetchJsonWithRetry(
                        restTemplate, buildForecastUrl(area), label, area.getId());

                if (response == null) {
                    failed++;
                } else {
                    insertIgnoreDuplicate(area, response.path("hourly"));
                    success++;
                }
            } catch (Exception e) {
                log.error("[{}] ERROR area={}", label, area.getId(), e);
                failed++;
            } finally {
                // Delay giữa mỗi request — tránh 429
                sleepQuietly(REQUEST_DELAY_MS);
            }
        }

        log.info("=== END [{}] — {}/{} success, {} failed ===",
                label, success, total, failed);
    }

    // =========================================================================
    // Helper: URL forecast với past + forecast trong 1 request
    // =========================================================================
    private String buildForecastUrl(Area area) {
        return UriComponentsBuilder
                .fromUriString(FORECAST_URL)
                .queryParam("latitude",      area.getLat())
                .queryParam("longitude",     area.getLon())
                .queryParam("past_days",     PAST_DAYS)
                .queryParam("forecast_days", FORECAST_DAYS)
                .queryParam("hourly",        HOURLY_FIELDS)
                .queryParam("timezone",      TIMEZONE)
                .toUriString();
    }

    private void insertIgnoreDuplicate(Area area, JsonNode hourly) {
        JsonNode times = hourly.path("time");
        if (!times.isArray() || times.isEmpty()) {
            log.warn("EMPTY HOURLY area={}", area.getId());
            return;
        }

        List<WeatherData> batch = new ArrayList<>(times.size());
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            batch.add(buildWeatherData(area, hourly, i, time));
        }

        // Thử saveAll — nếu batch có bất kỳ duplicate nào, fallback insert từng record
        try {
            weatherDataRepository.saveAll(batch);
            log.info("INSERTED {} records area={}", batch.size(), area.getId());
        } catch (Exception e) {
            // Partial duplicate: saveAll rollback toàn batch → fallback từng record
            // Những record không duplicate sẽ được insert thành công.
            // Những record duplicate bị bỏ qua nhờ ON CONFLICT DO NOTHING.
            int inserted = 0;
            for (WeatherData wd : batch) {
                try {
                    weatherDataRepository.save(wd);
                    inserted++;
                } catch (Exception ex) {
                    // Duplicate — bỏ qua, không log để tránh spam
                }
            }
            log.info("FALLBACK {}/{} inserted area={}", inserted, batch.size(), area.getId());
        }
    }

    // =========================================================================
    // Helper: Retry với exponential backoff
    // =========================================================================
    private JsonNode fetchJsonWithRetry(RestTemplate restTemplate,
                                        String url,
                                        String scope,
                                        UUID areaId) {
        for (int attempt = 1; attempt <= RETRY_ATTEMPTS; attempt++) {
            try {
                return restTemplate.getForObject(url, JsonNode.class);

            } catch (HttpClientErrorException.TooManyRequests e) {
                long wait = (long) RETRY_BACKOFF_MS * attempt;
                log.warn("{} area={} → 429 attempt={}/{} wait={}ms",
                        scope, areaId, attempt, RETRY_ATTEMPTS, wait);
                sleepQuietly(wait);

            } catch (ResourceAccessException e) {
                long wait = (long) RETRY_BACKOFF_MS * attempt;
                log.warn("{} area={} → timeout attempt={}/{} wait={}ms",
                        scope, areaId, attempt, RETRY_ATTEMPTS, wait);
                sleepQuietly(wait);

            } catch (Exception e) {
                long wait = (long) RETRY_BACKOFF_MS * attempt;
                log.warn("{} area={} → error attempt={}/{} wait={}ms msg={}",
                        scope, areaId, attempt, RETRY_ATTEMPTS, wait, e.getMessage());
                sleepQuietly(wait);
            }
        }

        log.error("{} area={} → FAILED after {} attempts", scope, areaId, RETRY_ATTEMPTS);
        return null;
    }

    // =========================================================================
    // Helper: Build entity từ JSON tại index i
    // =========================================================================
    private WeatherData buildWeatherData(Area area, JsonNode hourly, int i, LocalDateTime time) {
        WeatherDataCreationRequest req = WeatherDataCreationRequest.builder()
                .precipitation(      decimal(hourly, "precipitation",             i))
                .temperature2m(      decimal(hourly, "temperature_2m",            i))
                .dewpoint2m(         decimal(hourly, "dew_point_2m",              i))
                .surfacePressure(    decimal(hourly, "surface_pressure",          i))
                .windspeed10m(       decimal(hourly, "wind_speed_10m",            i))
                .winddirection10m(   decimal(hourly, "wind_direction_10m",        i))
                .relativehumidity2m( decimal(hourly, "relative_humidity_2m",      i))
                .evapotranspiration( decimal(hourly, "et0_fao_evapotranspiration",i))
                .lat(area.getLat())
                .lon(area.getLon())
                .build();

        WeatherData wd = weatherDataMapper.toWeatherData(req);
        wd.setArea(area);
        wd.setTime(time);
        return wd;
    }

    // =========================================================================
    // Helper: Parse BigDecimal an toàn
    // =========================================================================
    private BigDecimal decimal(JsonNode node, String field, int index) {
        JsonNode arr = node.path(field);
        if (!arr.isArray() || index >= arr.size() || arr.get(index).isNull())
            return null;
        return BigDecimal.valueOf(arr.get(index).asDouble());
    }

    private RestTemplate buildRestTemplate() {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}