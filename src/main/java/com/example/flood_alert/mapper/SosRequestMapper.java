package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.entity.SosRequest;

@Mapper(componentModel = "spring")
public interface SosRequestMapper {

    @Mapping(target = "alreadyExists", ignore = true)
    SosResponse toResponse(SosRequest sos);
}
