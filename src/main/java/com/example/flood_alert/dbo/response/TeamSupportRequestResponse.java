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

// Team Leader theo dõi các yêu cầu hỗ trợ đã gửi
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamSupportRequestResponse {

    UUID id;

    UUID sosId;

    SupportRequestStatus status;

    String reason;

    LocalDateTime createdAt;

    LocalDateTime reviewedAt;

    List<TeamSupportRequestItemResponse> items;
}