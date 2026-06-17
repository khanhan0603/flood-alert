package com.example.flood_alert.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.AreaDetailResponse;
import com.example.flood_alert.entity.Area;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByTenkhuvuc(String tenkhuvuc);

    boolean existsByTenkhuvucAndParentId(String tenkhuvuc, UUID parentId);

    Optional<Area> findByTenkhuvuc(String tenkhuvuc);

    List<Area> findTop100ByLevelAndIdGreaterThanAndLatIsNotNullAndLonIsNotNullOrderById(int level, UUID id);

    // Thêm query này để JOIN FETCH parent, tránh N+1 và LazyInit
    @Query("SELECT a FROM Area a LEFT JOIN FETCH a.parent ORDER BY a.level ASC")
    List<Area> findAllByOrderByLevelAsc();

    @Query("""
                SELECT new com.example.flood_alert.dbo.response.AreaDataByParentResponse(
                    a.id,
                    a.tenkhuvuc
                )
                FROM Area a
                WHERE a.parent.id= :parentId
                    AND a.level=2
            """)
    List<AreaDataByParentResponse> findByParentId(UUID parentId);

    long countByLevel(int level);

    @Query("""
                SELECT a
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
    List<Area> findAreasWithoutWeather(Pageable pageable);

    List<Area> findByLevelAndLatIsNotNullAndLonIsNotNull(
            Integer level);

    @Query(value = """
                SELECT
                    a.id,
                    a.tenkhuvuc,
                    ST_AsGeoJSON(a.polygon) as geometry
                FROM areas a
                WHERE a.id= :id
            """, nativeQuery = true) // nativeQuery = true => dùng tên table thật trong DB
    Object findPolygonById(UUID id);

    @Query("""
            SELECT a FROM Area a
            WHERE a.level = 2
              AND a.lat IS NOT NULL
              AND a.lon IS NOT NULL
              AND EXISTS (
                  SELECT 1 FROM WeatherData w WHERE w.area = a
              )
              AND (
                  SELECT MAX(w.time) FROM WeatherData w WHERE w.area = a
              ) < :threshold
            """)
    List<Area> findAreasWithOutdatedWeather(
            @Param("threshold") LocalDateTime threshold,
            Pageable pageable);

    @Query(value = """
            SELECT a.* FROM areas a
            WHERE a.level = 2
              AND a.lat IS NOT NULL
              AND a.lon IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1 FROM weather_datas w
                  WHERE w.area_id = a.id
                    AND w.time >= :startDate
                    AND w.time < :endDate
              )
            """, nativeQuery = true)
    List<Area> findAreasMissingDataInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query(value = """
                SELECT *
                FROM areas
                WHERE unaccent(lower(tenkhuvuc))
                        LIKE CONCAT('%',unaccent(lower(:keyword)),'%')
                ORDER BY
                    CASE
                        WHEN unaccent(lower(tenkhuvuc)) LIKE CONCAT(unaccent(lower(:keyword)),'%')
                        THEN 0
                        ELSE 1
                    END,
                    tenkhuvuc
            """, countQuery = """
                SELECT COUNT(*)
                FROM areas
                WHERE unaccent(lower(tenkhuvuc))
                    LIKE CONCAT('%',unaccent(lower(:keyword)),'%')
            """, nativeQuery = true)
    Page<Area> searchArea(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            SELECT id
            FROM areas
            WHERE level = 2
              AND ST_Contains(
                    polygon,
                    ST_SetSRID(
                        ST_Point(:lon, :lat),
                        4326
                    )
              )
            LIMIT 1
            """, nativeQuery = true)
    UUID findAreaIdByLatLon(
            @Param("lat") BigDecimal lat,
            @Param("lon") BigDecimal lon);

    @Query("""
                SELECT a.id
                FROM Area a
                WHERE a.level = 2
            """)
    List<UUID> findAllAreaIds();

    // Detail area
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.AreaDetailResponse(
                    a.tenkhuvuc,
                    a.mota,
                    p.tenkhuvuc
                )
                FROM Area a
                LEFT JOIN a.parent p
                WHERE a.id = :areaId
            """)
    AreaDetailResponse findDetailArea(@Param("areaId") UUID areaId);
}
