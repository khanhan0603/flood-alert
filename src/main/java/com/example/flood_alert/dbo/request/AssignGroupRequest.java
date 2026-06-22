package com.example.flood_alert.dbo.request;
import java.util.UUID;

import com.example.flood_alert.enums.AssignmentRole;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader giao nhiệm vụ cho group
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignGroupRequest {
    UUID sosId;

    UUID groupId;

    AssignmentRole role;

    String note;
}
