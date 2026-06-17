package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.enums.StatusSOS;

public interface SosRequestRepository extends JpaRepository<SosRequest, UUID> {
    //Đã đăng nhập
    boolean existsByUserIdAndStatusIn(UUID userId,List<StatusSOS> statuses);

    // Chưa đăng nhập
    boolean existsBySodtAndStatusIn(String sodt,List<StatusSOS> statuses);
}