package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.CallTaskResponse;
import com.example.flood_alert.dbo.response.UpdateCallResultResponse;
import com.example.flood_alert.entity.CallTask;

@Mapper(componentModel = "spring")
public interface CallTaskMapper {

    @Mapping(target = "callTaskId", source = "id")
    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "targetUserName", source = "targetUser.hoten")
    @Mapping(target = "phoneNumber", source = "targetUser.sodt")
    @Mapping(target = "targetType", source = "targetType")
    @Mapping(target = "timeoutSeconds", source = "timeoutSeconds")
    @Mapping(target = "retryCount", source = "retryCount")
    CallTaskResponse toResponse(CallTask callTask);

    @Mapping(target = "callTaskId", source = "id")
    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "targetUserName", source = "targetUser.hoten")
    @Mapping(target = "phoneNumber", source = "targetUser.sodt")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "targetType", source = "targetType")
    @Mapping(target = "retryCount", source = "retryCount")
    @Mapping(target = "timeoutSeconds", source = "timeoutSeconds")
    UpdateCallResultResponse toUpdateCallResultResponse(CallTask entity);
}