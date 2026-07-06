package com.example.flood_alert.dbo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiPredictionResponse {

    String status;

    Integer total;

    Integer processed;

    @JsonProperty("high_risk")
    Integer highRisk;

    Integer errors;

    @JsonProperty("duration_ms")
    Long durationMs;

    Recovery recovery;
}