package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.entity.SupportRequest;

@Mapper(componentModel = "spring", uses = SupportRequestItemMapper.class)
public interface SupportRequestMapper {

    @Mapping(target = "sosId", source = "sos.id")

    @Mapping(target = "requestedById", source = "requestedBy.id")
    @Mapping(target = "requestedByName", source = "requestedBy.hoten")

    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.hoten")

    SupportRequestResponse toResponse(
            SupportRequest supportRequest);
}