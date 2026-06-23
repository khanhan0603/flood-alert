package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.ApproveSupportRequest;
import com.example.flood_alert.dbo.request.AssignSupportGroupRequest;
import com.example.flood_alert.dbo.request.CreateSupportRequest;
import com.example.flood_alert.dbo.request.RejectAssignedSupportRequest;
import com.example.flood_alert.dbo.request.RejectSupportRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.service.SupportRequestService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/support-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupportRequestController {
        SupportRequestService supportRequestService;

        // Tạo yêu cầu hỗ trợ cúu hộ, teamleader tạo
        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> create(
                        @RequestBody @Valid CreateSupportRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(supportRequestService.create(request))
                                .build();
        }

        // Danh sách các yêu cầu trạng thái 
        @GetMapping
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Page<SupportRequestResponse>> getByStatus(
                        @RequestParam SupportRequestStatus status,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse
                                .<Page<SupportRequestResponse>>builder()
                                .result(
                                                supportRequestService.getByStatus(
                                                                status,
                                                                pageable))
                                .build();
        }

        // Chi tiết yêu cầu
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<SupportRequestResponse> getDetail(
                        @PathVariable UUID id) {

                return ApiResponse
                                .<SupportRequestResponse>builder()
                                .result(
                                                supportRequestService
                                                                .getDetail(id))
                                .build();
        }

        @PutMapping("/{id}/approve")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Void> approve(
                        @PathVariable UUID id,
                        @RequestBody @Valid ApproveSupportRequest request) {

                supportRequestService.approve(id, request);

                return ApiResponse.<Void>builder()
                                .build();
        }

        @PutMapping("/{id}/reject")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Void> reject(
                        @PathVariable UUID id,
                        @RequestBody @Valid RejectSupportRequest request) {

                supportRequestService.reject(id, request);

                return ApiResponse.<Void>builder()
                                .build();
        }

        // Team leader xem danh sách yêu cầu đc tỉnh giao cho
        @GetMapping("/my-team")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<SupportRequestResponse>> getMyTeamSupportRequests(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse
                                .<Page<SupportRequestResponse>>builder()
                                .result(
                                                supportRequestService
                                                                .getMyTeamSupportRequests(
                                                                                pageable))
                                .build();
        }

        // Team leader giao nhiệm vụ hỗ trợ cho group của mình
        @PostMapping("/{id}/assign-group")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> assignGroup(
                        @PathVariable UUID id,
                        @RequestBody AssignSupportGroupRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(
                                                supportRequestService.assignGroup(
                                                                id,
                                                                request))
                                .build();
        }

        // Team leader từ chối
        @PutMapping("/{id}/team-reject")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Void> teamReject(
                        @PathVariable UUID id,
                        @RequestBody RejectAssignedSupportRequest request) {

                supportRequestService.teamReject(id, request);

                return ApiResponse.<Void>builder()
                                .build();
        }
}
