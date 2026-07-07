package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSupportRequestResponse {

    UUID id;

    String groupName;

    String groupLeaderName;

    String reason;

    SupportRequestStatus status;

    LocalDateTime createdAt;
}