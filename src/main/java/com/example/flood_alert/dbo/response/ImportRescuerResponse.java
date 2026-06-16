package com.example.flood_alert.dbo.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportRescuerResponse {
    private int success;
    private int failed;
    private List<RowError> errors;
}
