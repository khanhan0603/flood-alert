package com.example.flood_alert.dbo.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Recovery {

    Integer attempts;

    Integer recovered;

    Integer errors;

    @JsonProperty("remaining_missing")
    Integer remainingMissing;

    @JsonProperty("error_details")
    List<PredictionErrorResponse> errorDetails;
}