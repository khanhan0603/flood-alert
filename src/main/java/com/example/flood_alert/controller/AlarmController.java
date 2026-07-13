package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.AlarmResponse;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.service.AlarmService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlarmController {

    AlarmService alarmService;

    //Danh sách các alarm của team
    @GetMapping
    public ApiResponse<Page<AlarmResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.<Page<AlarmResponse>>builder()
                .result(alarmService.getMyTeamAlarms(pageable))
                .build();
    }
}