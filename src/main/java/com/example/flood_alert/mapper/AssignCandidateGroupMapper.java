package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.AssignCandidateGroupResponse;
import com.example.flood_alert.entity.RescueGroup;

@Mapper(componentModel = "spring")
public interface AssignCandidateGroupMapper {

    @Mapping(target = "leaderId", source = "leader.id")
    @Mapping(target = "leaderName", source = "leader.hoten")
    @Mapping(target = "memberCount", ignore = true)
    AssignCandidateGroupResponse toResponse(RescueGroup group);

}