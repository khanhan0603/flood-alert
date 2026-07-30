package com.example.flood_alert.dbo.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChangePasswordResponse {
    private String message;
}
