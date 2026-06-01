package com.example.flood_alert.dbo.response;

import com.example.flood_alert.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    String id;
    String hoten;
    String email;
    String sodt;
    Role role;
}
