package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.RescueGroupMember;
import com.example.flood_alert.entity.RescueGroupMemberId;

public interface RescueGroupMemberRepository extends JpaRepository<RescueGroupMember, RescueGroupMemberId> {
    // Kiểm tra nhóm cứu hộ đã có người này chưa
    boolean existsByUser_Id(UUID userId);
    // Kiểm tra người dùng nây thuộc nhóm cúu hộ này chưa
    boolean existsByGroup_IdAndUser_Id(UUID groupId,UUID userId);
}
