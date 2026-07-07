package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSupportRequestDetailResponse {

    UUID id;

    String groupName;

    String groupLeaderName;

    String reason;

    SupportRequestStatus status;

    LocalDateTime createdAt;

    List<GroupSupportRequestItemResponse> items;
}