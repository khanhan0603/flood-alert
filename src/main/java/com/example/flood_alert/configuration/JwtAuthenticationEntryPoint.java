package com.example.flood_alert.configuration;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);

        //Chuyển response thành utf-8
        response.setCharacterEncoding(
                "UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        apiResponse));
    }
}