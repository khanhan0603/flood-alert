package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.UserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.entity.User;


@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class UserController {
    UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request) {
       ApiResponse<User> apiResponse=new ApiResponse<>();
       apiResponse.setResult(userService.createUser(request));
       return apiResponse;
    }
    
}
