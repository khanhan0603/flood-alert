package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;

import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
}
