package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.Area;


public interface  AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByTenkhuvuc(String tenkhuvuc);
    boolean existsByTenkhuvucAndParent(String tenkhuvuc,Area parent);
    Optional<Area> findByTenkhuvuc(String tenkhuvuc);
    List<Area> findTop100ByLevelAndIdGreaterThanAndLatIsNotNullAndLonIsNotNullOrderById(int level,UUID id);
    // Thêm query này để JOIN FETCH parent, tránh N+1 và LazyInit
    @Query("SELECT a FROM Area a LEFT JOIN FETCH a.parent ORDER BY a.level ASC")
    List<Area> findAllByOrderByLevelAsc();
    long countByLevel(int level);
}
