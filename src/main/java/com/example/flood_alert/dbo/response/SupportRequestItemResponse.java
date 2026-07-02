package com.example.flood_alert.dbo.response;
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

//Thông tin chi tiết hỗ trợ
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportRequestItemResponse {
    UUID id;

    SupportType supportType;

    Integer requiredGroupCount;

    SupportRequestItemStatus status;

    UUID assignedTeamId;

    String assignedTeamName;

    String provinceNote;

    String teamResponse;
    Integer assignedGroupCount;
}
