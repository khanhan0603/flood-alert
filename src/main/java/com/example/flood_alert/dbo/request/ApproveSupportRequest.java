package com.example.flood_alert.dbo.request;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province duyệt support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveSupportRequest {
    UUID asignedGroupId;
    String provinceResponse;
}
