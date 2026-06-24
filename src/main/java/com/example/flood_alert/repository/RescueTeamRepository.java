package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.entity.RescueTeam;

public interface RescueTeamRepository extends JpaRepository<RescueTeam, UUID> {
    // Kiểm tra tồn tại tên đội cứu hộ
    boolean existsByName(String name);

    // Tìm team theo id leader
    Optional<RescueTeam> findByLeaderId(UUID leaderId);

    List<RescueTeam> findByAreaId(UUID areaId);

    // Thông tin của team theo id khuvuc
    Optional<RescueTeam> findByArea_Id(UUID areaId);

    // Detail a team
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.RescueTeamResponse(
                    rt.id,
                    rt.name,
                    rt.description,
                    rt.area.id,
                    rt.area.tenkhuvuc,
                    rt.leader.id,
                    rt.leader.hoten
                )
                FROM RescueTeam rt
                WHERE rt.id = :teamId
            """)
    RescueTeamResponse findDetail(UUID teamId);

    // List team by area level 1
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.RescueTeamResponse(
                    rt.id,
                    rt.name,
                    rt.description,
                    rt.area.id,
                    rt.area.tenkhuvuc,
                    rt.leader.id,
                    rt.leader.hoten
                )
                FROM RescueTeam rt
                WHERE rt.area.parent.id = :areaId
            """)
    Page<RescueTeamResponse> findByAreaId(UUID areaId, Pageable pageable);

    // Số team phụ trách của tỉnh
    @Query("""
                SELECT COUNT(rt)
                FROM RescueTeam rt
                WHERE rt.area.parent.id = :provinceId
            """)
    long countByProvinceId(UUID provinceId);

    @Query("""
                SELECT rt
                FROM RescueTeam rt
                WHERE rt.area.parent.id = :provinceId
            """)
    Page<RescueTeam> findByProvinceId(
            UUID provinceId,
            Pageable pageable);
}
