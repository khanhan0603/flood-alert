package com.example.flood_alert.dbo.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader tạo support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSupportRequest {

    @NotBlank(message="SOS_REQUIRED")
    UUID sosId;

    @NotBlank(message="REASON_REQUIRED")
    @Size(max=1000, message="REASON_TOO_LONG")
    String reason;

    @NotEmpty(message="SUPPORT_REQUEST_ITEM_REQUIRED")
    @Valid
    List<CreateSupportRequestItem> items;
}
