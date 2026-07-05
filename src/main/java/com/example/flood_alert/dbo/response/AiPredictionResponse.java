package com.example.flood_alert.dbo.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiPredictionResponse {

    String status;

    Integer total;

    Integer processed;

    Integer highRisk;

    Integer errors;

    Long durationMs;

    Recovery recovery;
}