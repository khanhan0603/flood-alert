package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.Priority;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupAssignmentResponse {
    UUID assignmentId;

    UUID sosId;

    AssignmentRole role;

    AssignmentStatus status;

    Priority priority;

    BigDecimal lat;

    BigDecimal lon;

    UUID primaryGroupId;

    String primaryGroupName;

    List<SupportGroupResponse> supportGroups;
}
