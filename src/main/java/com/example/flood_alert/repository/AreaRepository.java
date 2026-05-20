package com.example.flood_alert.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.Area;


public interface  AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByTenkhuvuc(String tenkhuvuc);
    boolean existsByTenkhuvucAndParent(String tenkhuvuc,Area parent);
    Optional<Area> findByTenkhuvuc(String tenkhuvuc);
}
