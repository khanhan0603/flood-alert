package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaSimpleResponse {
    UUID id;
    String tenkhuvuc;
    BigDecimal lat;
    BigDecimal lon;
    int level;
    @Builder.Default
    List<AreaSimpleResponse> children =
        new ArrayList<>();
}
