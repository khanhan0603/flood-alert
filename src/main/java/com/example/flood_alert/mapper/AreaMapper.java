package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.AreaSimpleResponse;
import com.example.flood_alert.entity.Area;

@Mapper(componentModel = "spring")
public interface AreaMapper{
    @Mapping(target = "children", ignore = true)
    AreaSimpleResponse toSimpleResponse(Area area);

}
