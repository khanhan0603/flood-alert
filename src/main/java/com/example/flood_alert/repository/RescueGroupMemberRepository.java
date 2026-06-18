package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.dbo.response.ListMemberOfGroupResponse;
import com.example.flood_alert.entity.RescueGroupMember;
import com.example.flood_alert.entity.RescueGroupMemberId;

public interface RescueGroupMemberRepository extends JpaRepository<RescueGroupMember, RescueGroupMemberId> {
    // Kiểm tra nhóm cứu hộ đã có người này chưa
    boolean existsByUser_Id(UUID userId);

    // Kiểm tra người dùng nây thuộc nhóm cúu hộ này chưa
    boolean existsByGroup_IdAndUser_Id(UUID groupId, UUID userId);

    // Danh sách các member trong group
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.ListMemberOfGroupResponse(
                    u.id,
                    u.hoten,
                    u.sodt,
                    CASE
                        WHEN rg.leader.id = u.id THEN true
                        ELSE false
                    END
                )
                FROM RescueGroupMember rgm
                JOIN rgm.user u
                JOIN rgm.group rg
                WHERE rg.id = :groupId
                ORDER BY
                    CASE
                        WHEN rg.leader.id = u.id THEN 0
                        ELSE 1
                    END,
                    u.hoten
            """)
    Page<ListMemberOfGroupResponse> findMembersByGroupId(UUID groupId,Pageable pageable);
}
