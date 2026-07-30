package com.example.flood_alert.dbo.request;

import com.example.flood_alert.enums.Status;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái không được để trống!")
    private Status status;
}