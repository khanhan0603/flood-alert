package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.SosChartResponse;
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
                        WHEN s.status = 'CANCELED' THEN 4
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
                  AND s.status IN (
                        com.example.flood_alert.enums.StatusSOS.PENDING,
                        com.example.flood_alert.enums.StatusSOS.PROCESSING
                  )
                ORDER BY s.createdAt DESC
            """)
    Page<SosRequest> findAnonymousActiveSos(
            @Param("sodt") String sodt,
            Pageable pageable);

    // Tìm các yêu cầu theo đội cứu hộ, sắp xếp theo trạng thái, ngày tạo mà các yêu
    // cầu đang xử lý hoặc chờ xử lý
    @Query("""
                SELECT s
                FROM SosRequest s
                WHERE s.team.id = :teamId
                  AND s.status IN (
                        com.example.flood_alert.enums.StatusSOS.PENDING,
                        com.example.flood_alert.enums.StatusSOS.PROCESSING
                  )
                ORDER BY
                    CASE
                        WHEN s.priority = 'CRITICAL' THEN 1
                        WHEN s.priority = 'HIGH' THEN 2
                        WHEN s.priority = 'MEDIUM' THEN 3
                        WHEN s.priority = 'LOW' THEN 4
                    END,
                    CASE
                        WHEN s.environmentRisk = 'HIGH' THEN 1
                        WHEN s.environmentRisk = 'MEDIUM' THEN 2
                        WHEN s.environmentRisk = 'LOW' THEN 3
                    END,
                    s.createdAt ASC
            """)
    Page<SosRequest> findActiveByTeamId(
            @Param("teamId") UUID teamId,
            Pageable pageable);

    // Các yêu cầu theo team và trạng thái
    Page<SosRequest> findByTeamIdAndStatus(UUID teamId, StatusSOS status, Pageable pageable);

    // Tìm các yêu cầu theo trạng thái
    Page<SosRequest> findByStatus(StatusSOS status, Pageable pageable);

    // Đếm số sos request của team theo status
    long countByTeamIdAndStatus(UUID teamId, StatusSOS status);

    /**
     * Tra cứu một SOS theo mã trackingCode.
     * Dùng cho API public tra cứu trạng thái SOS.
     */
    Optional<SosRequest> findByTrackingCode(String trackingCode);

    /**
     * Lấy danh sách SOS theo số điện thoại người yêu cầu cứu hộ.
     * Sắp xếp mới nhất trước.
     * Dùng khi Operator tra cứu các lần yêu cầu cứu hộ của người dân
     * khi họ gọi lại Hotline.
     */
    List<SosRequest> findBySodtOrderByCreatedAtDesc(String sodt);

    Optional<SosRequest> findFirstBySodtAndStatusIn(
            String sodt,
            Collection<StatusSOS> statuses);

    /**
     * Tìm kiếm SOS cho Hotline.
     * keyword có thể là:
     * - Số điện thoại
     * - Mã tracking
     *
     * status là điều kiện lọc không bắt buộc.
     */
    @Query("""
                SELECT s
                FROM SosRequest s
                WHERE
                    (
                        :keyword IS NULL
                        OR :keyword = ''
                        OR s.sodt = :keyword
                        OR UPPER(s.trackingCode) = UPPER(:keyword)
                    )
                AND
                    (
                        :status IS NULL
                        OR s.status = :status
                    )
                ORDER BY s.createdAt DESC
            """)
    Page<SosRequest> searchHotlineSos(
            @Param("keyword") String keyword,
            @Param("status") StatusSOS status,
            Pageable pageable);

    /**
     * Danh sách SOS do Operator nhập tay.
     */
    @Query("""
                SELECT s
                FROM SosRequest s
                WHERE
                    s.sosSource = com.example.flood_alert.enums.SosSource.HOTLINE_OPERATOR
                    AND s.linkedCallEvent IS NULL
                ORDER BY s.createdAt DESC
            """)
    Page<SosRequest> findManualHotlineSos(
            Pageable pageable);

    // THỐNG KÊ

    /**
     * Đếm số lượng SOS theo trạng thái.
     */
    long countByStatus(StatusSOS status);

    /**
     * Đếm số lượng SOS được tạo trong ngày hiện tại.
     * FUNCTION('DATE', ...) được sử dụng để lấy phần
     * ngày của createdAt, giúp thống kê số lượng SOS phát
     * sinh trong ngày hiện tại.
     */
    @Query("""
                SELECT COUNT(s)
                FROM SosRequest s
                WHERE FUNCTION('DATE', s.createdAt) = CURRENT_DATE
            """)
    long countTodaySos();

    /**
     * Đếm tổng số yêu cầu cứu hộ trong khoảng thời gian.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM sos_requests
            WHERE created_at >= :from
              AND created_at < :to
            """, nativeQuery = true)
    long countByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Đếm số yêu cầu cứu hộ theo trạng thái
     * trong khoảng thời gian.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM sos_requests
            WHERE status = :status
              AND created_at >= :from
              AND created_at < :to
            """, nativeQuery = true)
    long countByStatusAndCreatedAtBetween(
            @Param("status") String status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Thống kê số lượng yêu cầu cứu hộ theo từng ngày.
     */
    @Query(value = """
            SELECT
                DATE(created_at) AS date,
                COUNT(*) AS total_sos
            FROM sos_requests
            WHERE created_at >= :from
              AND created_at < :to
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
            """, nativeQuery = true)
    List<Object[]> getSosChart(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}