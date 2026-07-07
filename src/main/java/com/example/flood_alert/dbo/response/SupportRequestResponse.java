package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.SupportRequestStatus;

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

    List<SupportRequestItemResponse> items;

    String reason;

    UUID requestedById;

    String requestedByName;

    String requestedPhone;

    UUID approvedById;

    String approvedByName;

    LocalDateTime createdAt;

    LocalDateTime reviewedAt;
}
