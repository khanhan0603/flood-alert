package com.example.flood_alert.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.dbo.response.ListMemberOfGroupResponse;
import com.example.flood_alert.entity.RescueGroupMember;
import com.example.flood_alert.entity.RescueGroupMemberId;
import com.example.flood_alert.enums.RescueGroupType;

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
    Page<ListMemberOfGroupResponse> findMembersByGroupId(UUID groupId, Pageable pageable);

    Optional<RescueGroupMember> findByUserId(UUID userId);

    // Kiểm tra xem user có trong group ko, và lấy entity để delete
    Optional<RescueGroupMember> findByGroup_IdAndUser_Id(UUID groupId, UUID userId);

    //Tìm group type theo mã người dugf
    @Query("""
            select rg.type
            from RescueGroup rg
            where rg.leader.id = :userId
               or exists (
                    select 1
                    from RescueGroupMember rgm
                    where rgm.group = rg
                      and rgm.user.id = :userId
               )
            """)
    Optional<RescueGroupType> findGroupTypeByUserId(UUID userId);

    Integer countByGroup_Id(UUID groupId);
}
