package com.example.flood_alert.dbo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateRequest {
    @NotBlank(message="LOGIN_INFO_REQUIRED")
    String loginInfo;

    @NotBlank(message="PASSWORD_REQUIRED")
    String password;
}
