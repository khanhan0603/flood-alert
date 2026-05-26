package com.example.flood_alert.controller;

import java.text.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.flood_alert.dbo.request.AuthenticateRequest;
import com.example.flood_alert.dbo.request.IntrospectRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.AuthenticateResponse;
import com.example.flood_alert.dbo.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        var result=authenticationService.authenticate(request);
        return ApiResponse.<AuthenticateResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)throws ParseException, JOSEException{
        var result=authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
    
}
