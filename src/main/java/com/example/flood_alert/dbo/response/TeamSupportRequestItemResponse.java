package com.example.flood_alert.dbo.response;

import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.enums.SupportType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// Team Leader theo dõi từng hạng mục hỗ trợ đã gửi
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamSupportRequestItemResponse {

    UUID id;

    SupportType supportType;

    SupportRequestItemStatus status;

    Integer requiredGroupCount;

    Integer assignedGroupCount;

    String assignedTeamName;

    String provinceNote;

    String teamResponse;

    List<AssignedGroupResponse> assignedGroups;
}