package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.entity.SosRequest;

@Mapper(componentModel = "spring")
public interface SosRequestMapper {

    @Mapping(target = "alreadyExists", ignore = true)
    @Mapping(target = "callEventId", source = "linkedCallEvent.id")
    @Mapping(target = "dispatcherUserId", source = "dispatcherUser.id")
    @Mapping(target = "dispatcherName", source = "dispatcherUser.hoten")
    @Mapping(target = "dispatcherType", source = "dispatcherType")
    SosResponse toResponse(SosRequest sos);
}
