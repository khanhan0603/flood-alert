package com.example.flood_alert.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.AreaWeatherResponse;
import com.example.flood_alert.dbo.response.WDataResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.WeatherData;

import jakarta.transaction.Transactional;

public interface WeatherDataRepository extends JpaRepository<WeatherData, UUID> {

    boolean existsByAreaAndTime(Area area, LocalDateTime time);

    @Query("""
                SELECT DISTINCT w.area.id
                FROM WeatherData w
                WHERE w.area.id IN :areaIds
            """)
    List<UUID> findDistinctAreaIdsByAreaIdIn(@Param("areaIds") List<UUID> areaIds);

    @Query("""
                SELECT COUNT(a)
                FROM Area a
                WHERE a.level = 2
                AND a.lat IS NOT NULL
                AND a.lon IS NOT NULL
                AND NOT EXISTS (
                    SELECT 1
                    FROM WeatherData w
                    WHERE w.area.id = a.id
                )
            """)
    long countAreaWithoutWeatherData();

    @Query("""
                SELECT COUNT(DISTINCT w.area.id)
                FROM WeatherData w
                WHERE w.area.level = 2
            """)
    long countDistinctAreaId();

    @Query("""
                SELECT DISTINCT new com.example.flood_alert.dbo.response.AreaWeatherResponse(
                    w.area.id,
                    w.area.tenkhuvuc
                )
                FROM WeatherData w
                WHERE w.area.level = 2
            """)
    List<AreaWeatherResponse> findDistinctAreaIdAndTenKhuvuc();

    @Query("""
                SELECT new com.example.flood_alert.dbo.response.WDataResponse(
                    w.rainfall,
                    w.temperature,
                    w.dewpoint,
                    w.pressure,
                    w.wind_speed,
                    w.wind_direction,
                    w.humidity,
                    w.evapotranspiration,
                    w.time
                )
                FROM WeatherData w
                WHERE w.area.id = :areaId
                ORDER BY w.time DESC
            """)
    List<WDataResponse> findWeatherResponseByAreaId(@Param("areaId") UUID areaId);

    @Query("""
                SELECT MAX(w.time)
                FROM WeatherData w
                WHERE w.area.id = :areaId
            """)
    LocalDateTime findMaxTimeByAreaId(@Param("areaId") UUID areaId);

    @Query(value = """
                SELECT *
                FROM weather_datas w
                WHERE w.area_id = :areaId
                  AND w.time >= :start
                  AND w.time < :end
            """, nativeQuery = true)
    List<WeatherData> findWeatherDataByAreaAndTime(
            @Param("areaId") UUID areaId,
            @Param("start")  LocalDateTime start,
            @Param("end")    LocalDateTime end);

    // =========================================================================
    // UPSERT: Insert 1 record, nếu (area_id, time) đã tồn tại thì UPDATE
    //         các field thời tiết — observed sẽ ghi đè forecast cũ đúng cách.
    //
    // Yêu cầu migration trước khi dùng:
    //   CREATE UNIQUE INDEX IF NOT EXISTS uk_weather_area_time
    //       ON weather_datas (area_id, time);
    // =========================================================================
    @Modifying
    @Transactional
    @Query(value = """
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
            )
            VALUES (
                gen_random_uuid(),
                :areaId,
                :time,
                :rainfall,
                :temperature,
                :dewpoint,
                :pressure,
                :windSpeed,
                :windDirection,
                :humidity,
                :evapotranspiration
            )
            ON CONFLICT (area_id, time)
            DO UPDATE SET
                rainfall           = EXCLUDED.rainfall,
                temperature        = EXCLUDED.temperature,
                dewpoint           = EXCLUDED.dewpoint,
                pressure           = EXCLUDED.pressure,
                wind_speed         = EXCLUDED.wind_speed,
                wind_direction     = EXCLUDED.wind_direction,
                humidity           = EXCLUDED.humidity,
                evapotranspiration = EXCLUDED.evapotranspiration
            """, nativeQuery = true)
    void upsertOne(
            @Param("areaId")             UUID          areaId,
            @Param("time")               LocalDateTime time,
            @Param("rainfall")           BigDecimal    rainfall,
            @Param("temperature")        BigDecimal    temperature,
            @Param("dewpoint")           BigDecimal    dewpoint,
            @Param("pressure")           BigDecimal    pressure,
            @Param("windSpeed")          BigDecimal    windSpeed,
            @Param("windDirection")      BigDecimal    windDirection,
            @Param("humidity")           BigDecimal    humidity,
            @Param("evapotranspiration") BigDecimal    evapotranspiration
    );

    // Xoá data quá khứ cũ hơn cutoff — forecast (time > now) giữ nguyên
    @Modifying
    @Transactional
    @Query("""
                DELETE FROM WeatherData w
                WHERE w.time < :cutoff
            """)
    int deleteByTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}