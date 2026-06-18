package com.example.flood_alert.repository;

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

    
}
