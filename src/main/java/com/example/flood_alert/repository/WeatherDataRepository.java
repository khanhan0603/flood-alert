package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.WeatherData;

public interface WeatherDataRepository extends JpaRepository<WeatherData, UUID> {
    boolean existsByAreaAndTime(Area area, LocalDateTime time);

    @Query("SELECT DISTINCT w.area.id FROM WeatherData w WHERE w.area.id IN :areaIds")
    List<UUID> findDistinctAreaIdsByAreaIdIn(@Param("areaIds") List<UUID> areaIds);

    @Query("""
        SELECT COUNT(a) FROM Area a
        WHERE a.level = 2
        AND a.id NOT IN (SELECT DISTINCT w.area.id FROM WeatherData w)
    """)
    long countAreaWithoutWeatherData();
}