package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.RescuerResponse;
import com.example.flood_alert.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "areaName", source = "area.tenkhuvuc")
    RescuerResponse toRescuerResponse(User user);
}
