package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.request.UpdateAnonymousSosRequest;
import com.example.flood_alert.dbo.request.UpdateSosRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.service.SOSRequestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sos-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SosRequestController {
    SOSRequestService sosRequestService;

    @PostMapping
    public ApiResponse<SosResponse> create(
            @RequestBody CreateSosRequest request,
            HttpServletRequest httpRequest) {

        return ApiResponse.<SosResponse>builder()
                .result(sosRequestService.create(request, httpRequest))
                .build();
    }

    // Update cho người dân đã có tài khoản
    @PutMapping("/{sosId}")
    public ApiResponse<SosResponse> update(
            @PathVariable UUID sosId,
            @RequestBody @Valid UpdateSosRequest request,
            HttpServletRequest httpRequest) {

        return ApiResponse.<SosResponse>builder()
                .result(sosRequestService.update(
                        sosId,
                        request,
                        httpRequest))
                .build();
    }

    // Update cho người dân không có tài khoản
    @PutMapping("/{sosId}/anonymous")
    public ApiResponse<SosResponse> updateAnonymous(
            @PathVariable UUID sosId,
            @RequestBody @Valid UpdateAnonymousSosRequest request,
            HttpServletRequest httpRequest) {

        return ApiResponse.<SosResponse>builder()
                .result(
                        sosRequestService.updateAnonymous(
                                sosId,
                                request,
                                httpRequest))
                .build();
    }
}
