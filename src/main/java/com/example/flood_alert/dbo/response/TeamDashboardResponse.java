package com.example.flood_alert.dbo.response;

import com.example.flood_alert.enums.StatusSOS;

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
public class TeamDashboardResponse {
    StatusSOS pendingStatus;
    long pendingCount;
    StatusSOS assignedStatus;
    long assignedCount;
    StatusSOS processingStatus;
    long processingCount;
    StatusSOS doneStatus;
    long doneCount;
    StatusSOS canceledStatus;
    long canceledCount;

    long totalCount;
}
