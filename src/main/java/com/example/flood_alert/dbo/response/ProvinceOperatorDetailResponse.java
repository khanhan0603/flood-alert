package com.example.flood_alert.dbo.response;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProvinceOperatorDetailResponse {
    UUID id;

    String hoten;

    String sodt;

    String email;

    UUID areaId;

    String tenKhuVucPhuTrach;

    List<ManagedTeamResponse> teams;
}
