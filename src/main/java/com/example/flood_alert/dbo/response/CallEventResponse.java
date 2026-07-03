package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.CallEventStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallEventResponse {

    UUID id;

    UUID teamId;

    String teamName;

    BigDecimal callerLat;

    BigDecimal callerLon;

    String callerPhoneNumber;

    CallEventStatus status;

    LocalDateTime createdAt;

}