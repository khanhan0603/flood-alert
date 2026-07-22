package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.CreateRescueTeamRequest;
import com.example.flood_alert.dbo.request.UpdateRescueTeamLeaderRequest;
import com.example.flood_alert.dbo.request.UpdateRescueTeamRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ImportRescuerResponse;
import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.dbo.response.RescueTeamLeaderResponse;
import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.dbo.response.TeamLeaderItemResponse;
import com.example.flood_alert.service.RescueTeamService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/res-team")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RescueTeamController {
        RescueTeamService rescueTeamService;

        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<RescueTeamResponse> create(
                        @RequestBody @Valid CreateRescueTeamRequest request) {
                return ApiResponse.<RescueTeamResponse>builder()
                                .result(rescueTeamService.create(request))
                                .build();
        }

        @PostMapping(value = "/{teamId}/import-rescuers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<ImportRescuerResponse> importRescuers(
                        @PathVariable UUID teamId,
                        @RequestParam("file") MultipartFile file) {

                return ApiResponse.<ImportRescuerResponse>builder()
                                .result(
                                                rescueTeamService.importRescuers(teamId, file))
                                .build();
        }

        // @PutMapping("/{teamId}/leader")
        // @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        // public ApiResponse<TeamLeaderResponse> assignLeader(
        //                 @PathVariable UUID teamId,
        //                 @RequestBody AssignTeamLeaderRequest request) {

        //         return ApiResponse.<TeamLeaderResponse>builder()
        //                         .result(rescueTeamService.assignLeader(
        //                                         teamId,
        //                                         request))
        //                         .build();
        // }

        // Danh sách leader theo khu vực
        @GetMapping("/leader/{areaId}")
        public ApiResponse<List<TeamLeaderItemResponse>> getLeadersByArea(
                        @PathVariable UUID areaId) {

                System.out.println("AREA = " + areaId);

                return ApiResponse
                                .<List<TeamLeaderItemResponse>>builder()
                                .result(rescueTeamService.getLeadersByArea(areaId))
                                .build();
        }

        // Danh sách các group của team
        @GetMapping("{teamId}/group")
        public ApiResponse<Page<RescueGroupResponse>> getListGroupOfTeam(
                        @PathVariable UUID teamId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ApiResponse.<Page<RescueGroupResponse>>builder()
                                .result(rescueTeamService.getListGroupOfTeam(teamId, pageable))
                                .build();
        }

        @GetMapping("/detail/{teamId}")
        public ApiResponse<RescueTeamResponse> getDetailTem(@PathVariable UUID teamId) {
                return ApiResponse.<RescueTeamResponse>builder()
                                .result(rescueTeamService.getDetailTeam(teamId))
                                .build();
        }

        // List team by area level 1
        @GetMapping("/area/{areaId}")
        public ApiResponse<Page<RescueTeamResponse>> getListTeamByArea(
                        @PathVariable UUID areaId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ApiResponse.<Page<RescueTeamResponse>>builder()
                                .result(rescueTeamService.getListTeamByArea(areaId, pageable))
                                .build();
        }

        // cập nhật thông tin đội
        @PutMapping("/{teamId}")
        public ApiResponse<RescueTeamResponse> update(
                        @PathVariable UUID teamId,
                        @RequestBody @Valid UpdateRescueTeamRequest request) {

                return ApiResponse.<RescueTeamResponse>builder()
                                .result(rescueTeamService.update(teamId, request))
                                .build();
        }

        // Xóa member ra khỏi team, ko xóa group leader
        @DeleteMapping("/{teamId}/members/{userId}")
        public ApiResponse<Void> deleteMember(
                        @PathVariable UUID teamId,
                        @PathVariable UUID userId) {

                rescueTeamService.deleteMember(teamId, userId);

                return ApiResponse.<Void>builder().build();
        }

        // Cập nhật đội trưởng và đội phó
        @PutMapping("/{teamId}/leader")
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<RescueTeamLeaderResponse> updateLeader(
                        @PathVariable UUID teamId,
                        @Valid @RequestBody UpdateRescueTeamLeaderRequest request) {

                return ApiResponse.<RescueTeamLeaderResponse>builder()
                                .result(rescueTeamService.updateLeader(teamId, request))
                                .build();
        }
}
