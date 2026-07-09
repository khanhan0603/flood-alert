package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.AssignmentStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignedGroupResponse {

    UUID groupId;

    String groupName;

    AssignmentStatus status;
}