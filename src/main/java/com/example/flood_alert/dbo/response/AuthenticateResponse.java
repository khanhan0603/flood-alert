package com.example.flood_alert.dbo.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuthenticateResponse {
    String token;
    UUID id;
    UUID areaId;
    String hoten;
    String role;
    boolean authenticated;
    UUID teamId;
}
