package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.SosAssignmentResponse;
import com.example.flood_alert.entity.SosAssignment;

@Mapper(componentModel = "spring")
public interface SosAssignmentMapper {
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "groupLeaderPhone", source = "group.leader.sodt")
    @Mapping(target = "teamId", source = "group.team.id")
    @Mapping(target = "teamName", source = "group.team.name")
    SosAssignmentResponse toResponse(
            SosAssignment assignment);
}
