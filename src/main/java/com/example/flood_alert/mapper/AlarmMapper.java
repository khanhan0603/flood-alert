package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.AlarmResponse;
import com.example.flood_alert.entity.Alarm;

@Mapper(componentModel = "spring")
public interface AlarmMapper {

    @Mapping(target = "sosRequestId", source = "sosRequest.id")
    @Mapping(target = "trackingCode", source = "sosRequest.trackingCode")
    @Mapping(target = "callTaskId", source = "callTask.id")
    AlarmResponse toResponse(Alarm alarm);

}