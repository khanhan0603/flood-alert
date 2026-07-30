package com.example.flood_alert.dbo.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteProvinceOperatorRequest {

    @NotEmpty(message = "Danh sách điều phối viên không được để trống!")
    private Set<UUID> ids;
}