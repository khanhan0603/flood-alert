package com.example.flood_alert.dbo.request;
import com.example.flood_alert.enums.AssignmentStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Group leader cập nhật tiến độ
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAssignmentStatusRequest {
    AssignmentStatus status;
    String note;
}
