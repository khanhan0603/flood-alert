package com.example.flood_alert.dbo.response;

import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@lombok.Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupDetailResponse {

    UUID id;

    String name;

    RescueGroupStatus status;

    RescueGroupType type;

    UUID teamId;

    String teamName;

    boolean hasBoat;

    boolean hasMedical;

    boolean hasSearchRescue;

    boolean hasLogistics;

    long currentMember;

    int minMember;

    int maxMember;

    boolean enoughMember;

    String notes;

    GroupLeaderResponse leader;

    List<GroupMemberResponse> members;
}