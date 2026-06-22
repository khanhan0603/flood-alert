package com.example.flood_alert.dbo.request;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province từ chối support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RejectSupportRequest {
    String provinceResponse;
}
