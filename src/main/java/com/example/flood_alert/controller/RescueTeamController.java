package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.CreateRescueTeamRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ImportRescuerResponse;
import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.service.RescueTeamService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/res-team")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RescueTeamController {
    RescueTeamService rescueTeamService;

    @PostMapping
    public ApiResponse<RescueTeamResponse> create(
            @RequestBody @Valid CreateRescueTeamRequest request) {
        return ApiResponse.<RescueTeamResponse>builder()
                .result(rescueTeamService.create(request))
                .build();
    }

    @PostMapping(
            value = "/{teamId}/import-rescuers",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ImportRescuerResponse> importRescuers(
            @PathVariable UUID teamId,
            @RequestParam("file") MultipartFile file
    ) {

        return ApiResponse.<ImportRescuerResponse>builder()
                .result(
                        rescueTeamService.importRescuers(teamId, file)
                )
                .build();
    }
}
