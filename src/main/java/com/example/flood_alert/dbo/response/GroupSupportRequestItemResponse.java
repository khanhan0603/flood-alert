package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.enums.SupportType;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSupportRequestItemResponse {

    UUID id;

    SupportType supportType;

    Integer requiredGroupCount;

    Integer assignedGroupCount;

    Integer completedGroupCount;

    SupportRequestItemStatus status;
}
