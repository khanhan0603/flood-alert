package com.example.flood_alert.dbo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveFcmTokenRequest {
    private String token;
}