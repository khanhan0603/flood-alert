package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.RescueTeamLeaderResponse;
import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.entity.RescueTeam;

@Mapper(componentModel = "spring")
public interface RescueTeamMapper {
    @Mapping(target = "teamId", source = "id")
    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.hoten")
    @Mapping(target = "deputyLeaderId", source = "deputyLeader.id")
    @Mapping(target = "deputyLeaderName", source = "deputyLeader.hoten")
    RescueTeamLeaderResponse toLeaderResponse(RescueTeam rescueTeam);

    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "areaName", source = "area.tenkhuvuc")
    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.hoten")
    @Mapping(target = "deputyId", source = "deputyLeader.id")
    @Mapping(target = "deputyName", source = "deputyLeader.hoten")
    RescueTeamResponse toResponse(RescueTeam rescueTeam);

}
