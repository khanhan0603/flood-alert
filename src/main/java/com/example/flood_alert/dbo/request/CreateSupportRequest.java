package com.example.flood_alert.dbo.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader tạo support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSupportRequest {
    UUID sosId;

    @NotBlank
    String reason;

    @NotEmpty
    @Valid
    List<CreateSupportRequestItem> items;
}
