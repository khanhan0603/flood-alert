package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.RescueTeam;

public interface RescueTeamRepository extends JpaRepository<RescueTeam, UUID> {
    //Kiểm tra tồn tại tên đội cứu hộ
    boolean existsByName(String name);

    List<RescueTeam> findByAreaId(UUID areaId);
}
