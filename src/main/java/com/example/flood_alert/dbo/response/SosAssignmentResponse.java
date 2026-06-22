package com.example.flood_alert.dbo.response;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//phản hồi đơn giao nhiệm vụ sos
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosAssignmentResponse {
    UUID id;

    UUID groupId;

    String groupName;

    UUID teamId;

    String teamName;

    AssignmentRole role;

    AssignmentStatus status;

    String note;

    LocalDateTime assignedAt;

    LocalDateTime acknowledgedAt;

    LocalDateTime arrivedAt;

    LocalDateTime completedAt;
}
