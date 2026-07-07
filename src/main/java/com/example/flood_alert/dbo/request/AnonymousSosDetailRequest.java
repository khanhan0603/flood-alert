package com.example.flood_alert.dbo.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnonymousSosDetailRequest {

    String sodt;

    String clientDeviceId;
}