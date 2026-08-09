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
public class CancelAnonymousSosRequest {
    @NotBlank(message="SODT_REQUIRED")
    String sodt;

    @NotBlank(message="CLIENT_DEVICE_ID_REQUIRED")
    String clientDeviceId;
}
