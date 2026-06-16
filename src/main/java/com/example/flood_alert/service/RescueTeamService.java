package com.example.flood_alert.service;

import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.request.CreateRescueTeamRequest;
import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.RescueTeamRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RescueTeamService {
    RescueTeamRepository rescueTeamRepository;
    AreaRepository areaRepository;

    public RescueTeamResponse create(CreateRescueTeamRequest request) {

        if (rescueTeamRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.RESCUE_TEAM_EXISTED);
        }

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

        RescueTeam team = RescueTeam.builder()
                .name(request.getName())
                .description(request.getDescription())
                .area(area)
                .build();
        team = rescueTeamRepository.save(team);

        return RescueTeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .areaId(area.getId())
                .areaName(area.getTenkhuvuc())
                .build();
    }
}
