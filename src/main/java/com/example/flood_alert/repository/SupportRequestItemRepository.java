package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.SupportRequestItem;
import com.example.flood_alert.enums.SupportRequestItemStatus;

public interface SupportRequestItemRepository extends JpaRepository<SupportRequestItem, UUID> {
    // Danh sách các chi tiết yêu cầu theo trạng thái
    @Query("""
            SELECT i
            FROM SupportRequestItem i
            JOIN FETCH i.supportRequest sr
            JOIN FETCH sr.sos
            JOIN FETCH sr.requestedBy rb
            LEFT JOIN FETCH i.assignedTeam
            WHERE sr.sos.area.parent.id = :provinceId
              AND i.status = :status
            ORDER BY sr.createdAt DESC
            """)
    Page<SupportRequestItem> findByProvinceAndStatus(
            UUID provinceId,
            SupportRequestItemStatus status,
            Pageable pageable);
}
