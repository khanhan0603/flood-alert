package com.example.flood_alert.configuration;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {

        ErrorCode errorCode = ErrorCode.NO_PERMISSION;

        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN);

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