package com.example.flood_alert.dbo.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Province duyệt support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveSupportRequest {
    @NotEmpty
    @Valid
    List<ApproveSupportRequestItem> items;
}
