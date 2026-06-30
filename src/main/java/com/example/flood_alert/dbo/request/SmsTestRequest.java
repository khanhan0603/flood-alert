package com.example.flood_alert.dbo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTestRequest {

    private String phone;

    private String content;
}