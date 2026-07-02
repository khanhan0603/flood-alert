package com.example.flood_alert.dbo.request;

import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestItemStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province duyệt support request con
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveSupportRequestItem {
    @NotNull
    UUID supportRequestItemId;

    @NotNull
    SupportRequestItemStatus status;

    // Bắt buộc nếu APPROVED
    UUID assignedTeamId;

    // Bắt buộc nếu REJECTED
    String provinceResponse;
}
