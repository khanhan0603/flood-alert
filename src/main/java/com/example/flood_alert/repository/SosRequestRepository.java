package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.enums.StatusSOS;

public interface SosRequestRepository extends JpaRepository<SosRequest, UUID> {
    // Kiểm tra người dân đã đăng nhập đã gửi yêu cầu SOS chưa
    boolean existsByUserIdAndStatusIn(UUID userId, List<StatusSOS> statuses);

    // Kiểm tra người dân chưa đăng nhập đã gửi yêu cầu SOS chưa
    boolean existsBySodtAndClientDeviceIdAndStatusIn(String sodt, String clientDeviceId, List<StatusSOS> statuses);

    // Cập nhật trang thái yêu cầu SOS
    Optional<SosRequest> findFirstByUserIdAndStatusIn(
            UUID userId,
            List<StatusSOS> statuses);

    Optional<SosRequest> findFirstBySodtAndClientDeviceIdAndStatusIn(
            String sodt,
            String clientDeviceId,
            List<StatusSOS> statuses);

    // Tìm kiếm yêu cầu SOS theo Id và UserId
    Optional<SosRequest> findByIdAndUserId(
            UUID sosId,
            UUID userId);

    // Tìm kiếm yêu cầu SOS theo Id và Sodt và ClientDeviceId
    Optional<SosRequest> findByIdAndSodtAndClientDeviceId(
            UUID sosId,
            String sodt,
            String clientDeviceId);

    // Danh sách yêu cầu SOS theo UserId theo trạng thái
    @Query("""
                SELECT s
                FROM SosRequest s
                WHERE s.user.id = :userId
                ORDER BY
                    CASE
                        WHEN s.status = 'PENDING' THEN 1
                        WHEN s.status = 'PROCESSING' THEN 2
                        WHEN s.status = 'DONE' THEN 3
                        WHEN s.status = 'CANCELLED' THEN 4
                    END,
                    s.createdAt DESC
            """)
    Page<SosRequest> findMySos(
            @Param("userId") UUID userId,
            Pageable pageable);

    // Xem danh sách sos đang hoạt động để người lạ coi mình gửi sos có đc xử lý ko
    @Query("""
                SELECT s
                FROM SosRequest s
                WHERE s.anonymous = true
                  AND s.sodt = :sodt
                  AND s.clientDeviceId = :clientDeviceId
                  AND s.status IN (
                        com.example.flood_alert.enums.StatusSOS.PENDING,
                        com.example.flood_alert.enums.StatusSOS.PROCESSING
                  )
                ORDER BY s.createdAt DESC
            """)
    Page<SosRequest> findAnonymousActiveSos(
            @Param("sodt") String sodt,
            @Param("clientDeviceId") String clientDeviceId,
            Pageable pageable);
}