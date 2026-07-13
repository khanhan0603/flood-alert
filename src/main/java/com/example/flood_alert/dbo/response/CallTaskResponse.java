package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.CallTargetType;
import com.example.flood_alert.enums.CallTaskStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallTaskResponse {
    UUID callTaskId;

    UUID targetUserId;

    String targetUserName;

    String phoneNumber;

    CallTargetType targetType;

    Integer timeoutSeconds;

    Integer retryCount;

    CallTaskStatus status;
}
