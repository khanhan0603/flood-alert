package com.example.flood_alert.dbo.response;

import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// Thông tin nhóm cứu hộ hiển thị cho người dân
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenAssignmentResponse {

    String groupName;

    String groupLeaderName;

    String groupLeaderPhone;

    AssignmentStatus status;

    AssignmentRole role;
}