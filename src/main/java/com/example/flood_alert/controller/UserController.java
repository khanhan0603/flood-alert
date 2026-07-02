package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.UpdateUserRequest;
import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.MyProfileResponse;
import com.example.flood_alert.dbo.response.UserResponse;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.service.UserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        User user = userService.createUser(request);

        UserResponse response = UserResponse.builder()
                .id(user.getId().toString())
                .hoten(user.getHoten())
                .email(user.getEmail())
                .sodt(user.getSodt())
                .build();

        return ApiResponse.<UserResponse>builder()
                .result(response)
                .build();
    }

    //Cập nhật thông tin người dùng
    @PutMapping("/me")
    public ApiResponse<MyProfileResponse> updateMyProfile(@RequestBody UpdateUserRequest request) {
        return ApiResponse.<MyProfileResponse>builder()
                .result(userService.updateMyProfile(request))
                .build();
    }

    //Thông tin người dùng
    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile() {
        return ApiResponse.<MyProfileResponse>builder()
                .result(userService.getMyProfile())
                .build();
    }
}
