package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.flood_alert.enums.Priority;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosMarkerResponse {

    UUID sosId;

    BigDecimal lat;

    BigDecimal lon;

    Priority priority;
}