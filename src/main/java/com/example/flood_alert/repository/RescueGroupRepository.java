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
}
