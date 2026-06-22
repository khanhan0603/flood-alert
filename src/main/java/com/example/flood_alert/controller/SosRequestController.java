package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.AnonymousSosListRequest;
import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.request.UpdateAnonymousSosRequest;
import com.example.flood_alert.dbo.request.UpdateSosRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.SosDetailResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.TeamDashboardResponse;
import com.example.flood_alert.enums.StatusSOS;
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

        // List sos request xếp theo status
        @GetMapping("/my-sos")
        public ApiResponse<Page<SosResponse>> getMySos(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getMySos(
                                                                pageable))
                                .build();
        }

        // List danh sách yêu cầu đang hoạt động cho người lạ
        @PostMapping("/my-active-anonymous")
        public ApiResponse<Page<SosResponse>> getAnonymousActiveSos(

                        @Valid @RequestBody AnonymousSosListRequest request,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getAnonymousActiveSos(
                                                                request,
                                                                pageable))
                                .build();
        }

        // Dashboard cho team leader
        @GetMapping("/{teamId}/team/summary")
        public ApiResponse<TeamDashboardResponse> getMyDashboard(@PathVariable UUID teamId) {

                return ApiResponse
                                .<TeamDashboardResponse>builder()
                                .result(
                                                sosRequestService.getTeamDashboard(teamId))
                                .build();
        }

        //Danh sách các sos thuộc từng trạng thái của team
        @GetMapping("/team/{status}")
        public ApiResponse<Page<SosResponse>> getMyTeamSosByStatus(
                        @PathVariable StatusSOS status,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(sosRequestService.getMyTeamSosByStatus(status, pageable))
                                .build();
        }

        // Danh sách các sos của team
        @GetMapping("/team")
        public ApiResponse<Page<SosResponse>> getMyTeamSos(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getMyTeamSos(pageable))
                                .build();
        }
}
