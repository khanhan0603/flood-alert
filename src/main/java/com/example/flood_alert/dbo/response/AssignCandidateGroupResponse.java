package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignCandidateGroupResponse {

    UUID id;

    String name;

    RescueGroupType type;

    RescueGroupStatus status;

    Integer memberCount;

    UUID leaderId;

    String leaderName;

    // Group đã từng gọi thất bại trong SOS hiện tại
    Boolean callFailed;
}