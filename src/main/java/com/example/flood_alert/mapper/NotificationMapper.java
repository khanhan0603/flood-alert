package com.example.flood_alert.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.response.PopupNotificationResponse;
import com.example.flood_alert.entity.Notification;
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "sosId", source = "sos.id")
    @Mapping(target = "trackingCode", source = "sos.trackingCode")
    PopupNotificationResponse toPopupResponse(Notification notification);
}
