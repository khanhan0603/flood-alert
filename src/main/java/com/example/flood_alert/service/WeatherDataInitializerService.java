package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.flood_alert.entity.Area;
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
    static final String HOURLY_FIELDS = "precipitation,temperature_2m,dew_point_2m,surface_pressure,"
            + "wind_speed_10m,wind_direction_10m,relative_humidity_2m,"
            + "et0_fao_evapotranspiration";

    static final int PAST_DAYS = 8;
    static final int FORECAST_DAYS = 3;

    // 1000ms × 3321 area ≈ 55 phút/lần sync
    // Giảm xuống 500ms sau khi xác nhận không bị 429
    static final int REQUEST_DELAY_MS = 700;
    static final int RETRY_ATTEMPTS = 5;
    static final int RETRY_BACKOFF_MS = 5_000;

    WeatherDataRepository weatherDataRepository;
    AreaRepository areaRepository;
    RestTemplateBuilder restTemplateBuilder;
    JdbcTemplate jdbcTemplate;

    static final String UPSERT_SQL = """
                INSERT INTO weather_datas (
                    id,
                    area_id,
                    time,
                    rainfall,
                    temperature,
                    dewpoint,
                    pressure,
                    wind_speed,
                    wind_direction,
                    humidity,
                    evapotranspiration
                )VALUES(
                    gen_random_uuid(),
                    ?,?,?,?,?,?,?,?,?,?
                    )
                    ON CONFLICT (area_id,time)
                    DO UPDATE SET
                    rainfall=EXCLUDED.rainfall,
                    temperature=EXCLUDED.temperature,
                    dewpoint=EXCLUDED.dewpoint,
                    pressure=EXCLUDED.pressure,
                    wind_speed=EXCLUDED.wind_speed,
                    wind_direction=EXCLUDED.wind_direction,
                    humidity=EXCLUDED.humidity,
                    evapotranspiration=EXCLUDED.evapotranspiration
            """;

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
    // SCHEDULER 3: Cleanup 23:55 — xoá data quá khứ cũ hơn PAST_DAYS ngày
    // Forecast (time > now) giữ nguyên cho AI
    // =========================================================================
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deleteOldData() {
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
    // PUBLIC: Trigger thủ công (admin API / test)
    // =========================================================================
    public void triggerManualSync() {
        log.info("MANUAL SYNC TRIGGERED");
        syncAll("MANUAL");
    }

    /**
     * On-demand: Controller gọi khi user mở phường/xã mà data cũ hơn ngưỡng.
     * Chỉ tốn 1 request, không ảnh hưởng batch.
     */
    public boolean fetchOnDemand(Area area) {
        log.info("ON-DEMAND FETCH area={}", area.getId());
        RestTemplate restTemplate = buildRestTemplate();
        try {
            JsonNode response = fetchJsonWithRetry(
                    restTemplate, buildForecastUrl(area), "ON-DEMAND", area.getId());
            if (response == null)
                return false;
            upsertHourly(area, response.path("hourly"));
            log.info("ON-DEMAND SUCCESS area={}", area.getId());
            return true;
        } catch (Exception e) {
            log.error("ON-DEMAND ERROR area={}", area.getId(), e);
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
        int total = areas.size();
        int success = 0;
        int failed = 0;

        for (int i = 0; i < total; i++) {
            Area area = areas.get(i);
            log.info("[{}] {}/{} area={}", label, i + 1, total, area.getId());

            try {
                JsonNode response = fetchJsonWithRetry(
                        restTemplate, buildForecastUrl(area), label, area.getId());

                if (response == null) {
                    failed++;
                } else {
                    long start = System.currentTimeMillis();
                    upsertHourly(area, response.path("hourly"));
                    log.info("[{}] UPSERT area={} took {} ms", label, area.getId(), System.currentTimeMillis() - start);
                    success++;
                }
            } catch (Exception e) {
                log.error("[{}] ERROR area={}", label, area.getId(), e);
                failed++;
            } finally {
                sleepQuietly(REQUEST_DELAY_MS);
            }
        }

        log.info("=== END [{}] — {}/{} success, {} failed ===",
                label, success, total, failed);
    }

    // =========================================================================
    // SAVE: Upsert 264 giờ/area bằng native SQL — không dùng JPA entity pipeline
    // ON CONFLICT DO UPDATE → observed ghi đè forecast cũ đúng cách
    // =========================================================================
    private void upsertHourly(Area area, JsonNode hourly) {
        JsonNode times = hourly.path("time");

        if (!times.isArray() || times.isEmpty()) {
            return;
        }

        int count = times.size();

        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LocalDateTime time = LocalDateTime.parse(times.get(i).asText());

                ps.setObject(1, area.getId());
                ps.setObject(2, time);
                ps.setBigDecimal(3, decimal(hourly, "precipitation", i));
                ps.setBigDecimal(4, decimal(hourly, "temperature_2m", i));
                ps.setBigDecimal(5, decimal(hourly, "dew_point_2m", i));
                ps.setBigDecimal(6, decimal(hourly, "surface_pressure", i));
                ps.setBigDecimal(7, decimal(hourly, "wind_speed_10m", i));
                ps.setBigDecimal(8, decimal(hourly, "wind_direction_10m", i));
                ps.setBigDecimal(9, decimal(hourly, "relative_humidity_2m", i));
                ps.setBigDecimal(10, decimal(hourly, "et0_fao_evapotranspiration", i));
            }

            @Override
            public int getBatchSize() {
                return count;
            }

        });
    }

    // =========================================================================
    // Helper: Build URL forecast — past_days + forecast_days trong 1 request
    // =========================================================================
    private String buildForecastUrl(Area area) {
        return UriComponentsBuilder
                .fromUriString(FORECAST_URL)
                .queryParam("latitude", area.getLat())
                .queryParam("longitude", area.getLon())
                .queryParam("past_days", PAST_DAYS)
                .queryParam("forecast_days", FORECAST_DAYS)
                .queryParam("hourly", HOURLY_FIELDS)
                .queryParam("timezone", TIMEZONE)
                .toUriString();
    }

    // =========================================================================
    // Helper: Retry với exponential backoff — dừng hẳn nếu hết daily quota
    // =========================================================================
    private JsonNode fetchJsonWithRetry(RestTemplate restTemplate,
            String url,
            String scope,
            UUID areaId) {
        for (int attempt = 1; attempt <= RETRY_ATTEMPTS; attempt++) {
            try {
                return restTemplate.getForObject(url, JsonNode.class);

            } catch (HttpClientErrorException.TooManyRequests e) {
                String body = e.getResponseBodyAsString();
                if (body.contains("Daily API request limit exceeded")) {
                    log.error("{} DAILY LIMIT REACHED — stop sync", scope);
                    return null;
                }
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
    // Helper: Parse BigDecimal an toàn từ hourly array
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

    public void refillWeather(UUID areaId) {

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found"));

        fetchOnDemand(area);
    }
}