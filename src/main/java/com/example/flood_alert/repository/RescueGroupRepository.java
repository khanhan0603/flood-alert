package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;

public interface RescueGroupRepository extends JpaRepository<RescueGroup, UUID> {
     boolean existsByTeamIdAndName(UUID teamId, String name);

     @Query("""
                  SELECT new com.example.flood_alert.dbo.response.RescueGroupResponse
                       (rg.id,
                       rg.name,
                       rg.team.id,
                       rg.team.name,
                       rg.status,
                       rg.hasBoat,
                       rg.hasMedical,
                       rg.hasSearchRescue,
                       rg.hasLogistics,
                       rg.notes)
                  FROM RescueGroup rg
                  WHERE rg.team.id = :teamId
               """)
     Page<RescueGroupResponse> findGroupByTeamId(UUID teamId, Pageable pageable);

     List<RescueGroup> findByTeamId(UUID teamId);

     List<RescueGroup> findByTeamIdAndStatus(UUID teamId, String status);

     Optional<RescueGroup> findByLeaderId(UUID leaderId);

     // Số group của 1 đội
     long countByTeamId(UUID teamId);

     // Kiem tra co phai group leader khong
     boolean existsByLeaderId(UUID leaderId);

     // Số lượng group boat của team
     long countByTeamIdAndHasBoatTrueAndStatus(
               UUID teamId,
               RescueGroupStatus status);

     // Số lượng group medical của team
     long countByTeamIdAndHasMedicalTrueAndStatus(
               UUID teamId,
               RescueGroupStatus status);

     // Số lượng group search rescue của team
     long countByTeamIdAndHasSearchRescueTrueAndStatus(
               UUID teamId,
               RescueGroupStatus status);

     // Số lượng group logistics của team
     long countByTeamIdAndHasLogisticsTrueAndStatus(
               UUID teamId,
               RescueGroupStatus status);

     boolean existsByTeamIdAndType(UUID teamId, RescueGroupType type);

     // Danh sách các group đang available theo loại yêu cầu hỗ trợ cần
     List<RescueGroup> findByTeam_IdAndStatusAndHasBoatTrue(
               UUID teamId,
               RescueGroupStatus status);

     List<RescueGroup> findByTeam_IdAndStatusAndHasMedicalTrue(
               UUID teamId,
               RescueGroupStatus status);

     List<RescueGroup> findByTeam_IdAndStatusAndHasSearchRescueTrue(
               UUID teamId,
               RescueGroupStatus status);

     List<RescueGroup> findByTeam_IdAndStatusAndHasLogisticsTrue(
               UUID teamId,
               RescueGroupStatus status);

}
