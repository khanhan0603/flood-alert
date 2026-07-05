package com.example.flood_alert.dbo.response;

import java.time.LocalDate;

import com.example.flood_alert.enums.PredictionJobStatus;
import com.example.flood_alert.enums.PredictionJobType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionJobHistoryResponse {

    String id;

    LocalDate date;

    PredictionJobType jobType;

    PredictionJobStatus status;
}