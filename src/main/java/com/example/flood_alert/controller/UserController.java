package com.example.flood_alert.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorResponse;
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

    // Danh sách các province
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ApiResponse<Page<ProvinceOperatorResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse
                .<Page<ProvinceOperatorResponse>>builder()
                .result(
                        userService.getAll(pageable))
                .build();
    }

}
