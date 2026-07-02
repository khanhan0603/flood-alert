package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.enums.SupportRequestStatus;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, UUID> {
        // Danh sách yêu cầu hỗ trợ theo trạng thái theo tỉnh chỉ provice tỉnh đó mới
        // xem đc
        @Query("""
                        SELECT sr
                        FROM SupportRequest sr
                        WHERE sr.sos.area.parent.id = :provinceId
                          AND sr.status = :status
                        """)
        Page<SupportRequest> findByProvinceAndStatus(
                        UUID provinceId,
                        SupportRequestStatus status,
                        Pageable pageable);

        // Team leader xem các yêu cầu hỗ trợ đc tỉnh giao cho
        @Query("""
                        SELECT DISTINCT sr
                        FROM SupportRequest sr
                        JOIN sr.items i
                        WHERE i.assignedTeam.id = :teamId
                        """)
        Page<SupportRequest> findMySupportRequests(
                        UUID teamId,
                        Pageable pageable);

        // Danh sách yêu cầu hỗ trợ theo sos id
        List<SupportRequest> findBySosId(UUID sosId);

        // Danh sách yêu cầu hỗ trợ theo Id yêu cầu
        List<SupportRequest> findByRequestedById(UUID requetedById);

        // Kiểm tra xem đã tạo hỗ trợ cho sos đó chưa
        boolean existsBySosIdAndStatus(UUID sosId, SupportRequestStatus status);
}
