package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.SupportRequestItemResponse;
import com.example.flood_alert.entity.SupportRequestItem;

@Mapper(componentModel = "spring")
public interface SupportRequestItemMapper {

    @Mapping(target = "assignedTeamId", source = "assignedTeam.id")
    @Mapping(target = "assignedTeamName", source = "assignedTeam.name")
    SupportRequestItemResponse toResponse(
            SupportRequestItem item);
}