package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.enums.SupportType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProvinceSupportItemResponse {
    //mức độ nguy hiểm của sos
    Priority priority;
    UUID itemId;

    UUID supportRequestId;

    UUID sosId;

    SupportType supportType;

    SupportRequestItemStatus status;

    Integer requiredGroupCount;

    Integer assignedGroupCount;

    String requesterTeamName;

    String assignedTeamName;

    String provinceNote;

    String teamResponse;

    LocalDateTime createdAt;
}