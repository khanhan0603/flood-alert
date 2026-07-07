package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.RescueGroupStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportCandidateGroupResponse {

    UUID id;

    String groupName;

    String leaderName;

    RescueGroupStatus status;

    boolean hasBoat;

    boolean hasMedical;

    boolean hasSearchRescue;

    boolean hasLogistics;
}
