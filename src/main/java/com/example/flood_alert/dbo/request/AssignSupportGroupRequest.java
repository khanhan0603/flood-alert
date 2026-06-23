package com.example.flood_alert.dbo.request;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader giao nhiệm vụ hỗ trợ cho group
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignSupportGroupRequest {
    UUID groupId;
    String note;
}
