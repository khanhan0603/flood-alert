package com.example.flood_alert.dbo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SmsRequest {
    @JsonProperty("ApiKey")
    String apiKey; // API Key của eSMS.

    @JsonProperty("SecretKey")
    String secretKey; // Secret Key.

    @JsonProperty("Phone")
    String phone; // Số điện thoại nhận.

    @JsonProperty("Content")
    String content; // Nội dung tin nhắn.

    // 2 = SMS Brandname CSKH.
    // 8 = SMS qua đầu số cố định/template
    @JsonProperty("SmsType")
    Integer smsType;
}
