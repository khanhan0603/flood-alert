package com.example.flood_alert.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.entity.Area;


public interface  AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByTenkhuvuc(String tenkhuvuc);
    boolean existsByTenkhuvucAndParentId(String tenkhuvuc,UUID parentId);
    Optional<Area> findByTenkhuvuc(String tenkhuvuc);
    List<Area> findTop100ByLevelAndIdGreaterThanAndLatIsNotNullAndLonIsNotNullOrderById(int level,UUID id);
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
        Integer level
    );
}
