package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.GroupSupportRequestResponse;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.enums.SupportRequestSource;
import com.example.flood_alert.enums.SupportRequestStatus;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, UUID> {
    // Danh sách yêu cầu hỗ trợ theo trạng thái theo tỉnh chỉ provice tỉnh đó mới
    // xem đc
    @Query("""
            SELECT sr
            FROM SupportRequest sr
            WHERE sr.source = :source
              AND sr.status = :status
              AND sr.sos.area.parent.id = :provinceId
            ORDER BY sr.createdAt DESC
            """)
    Page<SupportRequest> findByProvinceAndStatus(
            @Param("provinceId") UUID provinceId,
            @Param("source") SupportRequestSource source,
            @Param("status") SupportRequestStatus status,
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

    // Kiểm tra Group đã có yêu cầu hỗ trợ đang chờ xử lý cho SOS này chưa
    @Query("""
                SELECT COUNT(sr) > 0
                FROM SupportRequest sr
                WHERE sr.sos.id = :sosId
                  AND sr.requestedBy.id = :requestedById
                  AND sr.source = :source
                  AND sr.status = :status
            """)
    boolean existsPendingGroupSupportRequest(
            UUID sosId,
            UUID requestedById,
            SupportRequestSource source,
            SupportRequestStatus status);

    // Danh sách các support request đc group leader gửi đến team theo trạng thái
    // support
    @Query("""
            SELECT new com.example.flood_alert.dbo.response.GroupSupportRequestResponse(
            sr.id,
            rg.name,
            sr.requestedBy.hoten,
            sr.reason,
            sr.status,
            sr.createdAt)
              FROM SupportRequest sr
              JOIN RescueGroup rg
                  ON rg.leader.id = sr.requestedBy.id
              WHERE sr.source = :source
                AND sr.status = :status
                AND sr.requestedBy.team.id = :teamId
              ORDER BY sr.createdAt DESC
              """)
    Page<GroupSupportRequestResponse> findGroupSupportRequests(
            @Param("source") SupportRequestSource source,
            @Param("status") SupportRequestStatus status,
            @Param("teamId") UUID teamId,
            Pageable pageable);
}
