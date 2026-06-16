package com.example.flood_alert.dbo.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RowError {
    private int row;
    private String message;
}
