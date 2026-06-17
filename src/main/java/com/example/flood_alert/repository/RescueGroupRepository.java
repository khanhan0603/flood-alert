package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.RescueGroup;

public interface RescueGroupRepository extends JpaRepository<RescueGroup, UUID> {
    boolean existsByTeamIdAndName(UUID teamId,String name);
}
