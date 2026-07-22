package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.enums.AssignmentStatus;

public interface SosAssignmentRepository extends JpaRepository<SosAssignment, UUID> {
  // Tìm kiếm danh sách nhiệm vụ theo id sos
  List<SosAssignment> findBySosId(UUID sosId);

  // Tìm kiếm danh sách nhiệm vuy theo id group
  List<SosAssignment> findByGroupId(UUID groupId);

  // Tìm kiếm danh sách nhiệm vụ theo id group leader
  @Query("""
          SELECT sa
          FROM SosAssignment sa
          WHERE sa.group.leader.id= :leaderId
      """)
  List<SosAssignment> findByGroupLeaderId(UUID leaderId);

  // Danh sách nhiệm vụ theo trạng thái
  List<SosAssignment> findByStatus(AssignmentStatus status);

  // Kiểm tra xem đã giao yêu cầu sos cho group nào chưa
  boolean existsBySosIdAndGroupId(UUID sosId, UUID groupId);

  // Danh sách nhiệm vụ của group
  @Query("""
          SELECT sa
          FROM SosAssignment sa
          WHERE sa.group.leader.id = :leaderId
          ORDER BY sa.sos.priority DESC,
                   sa.assignedAt DESC
      """)
  List<SosAssignment> findMyAssignments(UUID leaderId);

  // Tìm thông tin của group chính
  @Query("""
          SELECT sa
          FROM SosAssignment sa
          WHERE sa.sos.id = :sosId
            AND sa.role = com.example.flood_alert.enums.AssignmentRole.PRIMARY
      """)
  Optional<SosAssignment> findPrimaryAssignment(UUID sosId);

  // Tìm thông tin group support
  @Query("""
          SELECT sa
          FROM SosAssignment sa
          WHERE sa.sos.id = :sosId
            AND sa.role = com.example.flood_alert.enums.AssignmentRole.SUPPORT
      """)
  List<SosAssignment> findSupportAssignments(UUID sosId);

  long countBySupportRequestItemIdAndStatus(
      UUID supportRequestItemId,
      AssignmentStatus status);

  @Query("""
          SELECT sa
          FROM SosAssignment sa
          WHERE sa.supportRequestItem.id = :supportRequestItemId
      """)
  List<SosAssignment> findBySupportRequestItemId(UUID supportRequestItemId);

  //Danh sách group mới gọi thất bại
  Optional<SosAssignment> findFirstBySos_IdAndGroup_IdOrderByAssignedAtDesc(UUID sosId,UUID groupId);
}
