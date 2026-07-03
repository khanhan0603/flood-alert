package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.CallEventResponse;
import com.example.flood_alert.entity.EmergencyCallEvent;

@Mapper(componentModel = "spring")
public interface EmergencyCallEventMapper {

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    CallEventResponse toResponse(EmergencyCallEvent entity);

}