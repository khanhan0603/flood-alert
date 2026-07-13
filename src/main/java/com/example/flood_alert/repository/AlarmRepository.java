package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.entity.Alarm;

public interface AlarmRepository extends JpaRepository<Alarm, UUID> {

    // Danh sách Alarm của một SOS
    List<Alarm> findBySosRequestIdOrderByCreatedAtDesc(UUID sosRequestId);

    // Danh sách Alarm của một CallTask
    List<Alarm> findByCallTaskIdOrderByCreatedAtDesc(UUID callTaskId);

    // Danh sách alarm theo team id
    @Query("""
                SELECT a
                FROM Alarm a
                WHERE a.sosRequest.team.id = :teamId
                ORDER BY a.createdAt ASC
            """)
    Page<Alarm> findAlarmByTeamId(UUID teamId, Pageable pageable);

}