package com.example.flood_alert.dbo.response;

import java.time.LocalDate;
import java.util.UUID;

import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RescuerResponse {

    private UUID id;

    private String hoten;

    private String email;

    private String sodt;

    private Boolean gioitinh;

    private LocalDate ngaysinh;

    private String diachi;

    private Role role;

    private Status trangthai;

    private UUID teamId;

    private String teamName;

    private UUID areaId;

    private String areaName;
}