package com.example.flood_alert.dbo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "OTP_REQUIRED")
    private String token;

    @NotBlank(message = "NEW_PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    private String newPassword;
}