package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.enums.SupportType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province xem support request
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportRequestResponse {
    UUID id;

    UUID sosId;

    SupportRequestStatus status;

    SupportType supportType;

    String reason;

    UUID requestedById;

    String requestedByName;

    UUID assignedTeamId;
    String assignedTeamName;

    UUID approvedById;

    String approvedByName;

    String provinceResponse;

    String teamResponse;

    LocalDateTime createdAt;

    LocalDateTime reviewedAt;
}
