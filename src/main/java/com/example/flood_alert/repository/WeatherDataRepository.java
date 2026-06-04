package com.example.flood_alert.repository;

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

public interface WeatherDataRepository
                extends JpaRepository<WeatherData, UUID> {

        boolean existsByAreaAndTime(
                        Area area,
                        LocalDateTime time);

        @Query("""
                            SELECT DISTINCT w.area.id
                            FROM WeatherData w
                            WHERE w.area.id IN :areaIds
                        """)
        List<UUID> findDistinctAreaIdsByAreaIdIn(
                        @Param("areaIds") List<UUID> areaIds);

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
        List<WDataResponse> findWeatherResponseByAreaId(
                        @Param("areaId") UUID areaId);

        @Query("""
                        SELECT MAX(time)
                        FROM WeatherData
                        WHERE area.id= :areaId
                        """)
        LocalDateTime findMaxTimeByAreaId(@Param("areaId") UUID areaId);

        // Đếm số area có đủ 24h data trong 1 ngày
        @Query(value = """
                        SELECT COUNT(*)
                        FROM (
                            SELECT w.area_id
                            FROM weather_datas w
                            WHERE w.time >= :start
                              AND w.time < :end
                            GROUP BY w.area_id
                            HAVING COUNT(*) >= :requiredHours
                        ) t
                        """, nativeQuery = true)
        long countAreasWithFullDay(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("requiredHours") int requiredHours);

        // Lấy danh sách giờ đã có của 1 area trong khoảng thời gian
        @Query("""
                            SELECT w.area.id, w.time
                            FROM WeatherData w
                            WHERE w.area.id IN :areaIds
                              AND w.time >= :start
                              AND w.time < :end
                        """)
        List<Object[]> findExistingTimesBatch(
                        @Param("areaIds") List<UUID> areaIds,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // Xóa data cũ hơn 8 ngày
        @Modifying
        @Transactional
        @Query("""
                                DELETE FROM WeatherData w
                                WHERE w.time < :cutoff
                        """)
        int deleteByTimeBefore(@Param("cutoff") LocalDateTime cutoff);

        @Query(value="""
                SELECT *
                FROM weather_datas w
                WHERE w.area_id = :area AND w.time >= :start
                  AND w.time <= :end
        """,nativeQuery=true)
        List<WeatherData> findWeatherDataByAreaAndTime(
                        @Param("area") UUID area,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

}