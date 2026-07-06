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
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ProvinceSupportItemResponse;
import com.example.flood_alert.dbo.response.SupportMapResponse;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.enums.SupportRequestItemStatus;
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

        // Team Leader tạo yêu cầu chi viện
        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> create(
                        @RequestBody @Valid CreateSupportRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(supportRequestService.create(request))
                                .build();
        }

        // Province xem danh sách yêu cầu
        @GetMapping
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Page<SupportRequestResponse>> getByStatus(
                        @RequestParam SupportRequestStatus status,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SupportRequestResponse>>builder()
                                .result(supportRequestService.getByStatus(status, pageable))
                                .build();
        }

        // Province xem chi tiết
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<SupportRequestResponse> getDetail(
                        @PathVariable UUID id) {

                return ApiResponse.<SupportRequestResponse>builder()
                                .result(supportRequestService.getDetail(id))
                                .build();
        }

        // Province duyệt các item trong Support Request
        @PutMapping("/{id}/approve")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Void> approve(
                        @PathVariable UUID id,
                        @RequestBody @Valid ApproveSupportRequest request) {

                supportRequestService.approve(id, request);

                return ApiResponse.<Void>builder().build();
        }

        // Team Leader xem các Support Request được giao cho Team mình
        @GetMapping("/my-team")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<SupportRequestResponse>> getMyTeamSupportRequests(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SupportRequestResponse>>builder()
                                .result(supportRequestService.getMyTeamSupportRequests(pageable))
                                .build();
        }

        // Team Leader phân Group cho một SupportRequestItem
        @PostMapping("/items/{itemId}/assign-group")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> assignGroup(
                        @PathVariable UUID itemId,
                        @RequestBody @Valid AssignSupportGroupRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(supportRequestService.assignGroup(itemId, request))
                                .build();
        }

        // Team Leader từ chối hạng mục hỗ trợ
        @PutMapping("/items/{itemId}/team-reject")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Void> teamReject(
                        @PathVariable UUID itemId,
                        @RequestBody @Valid RejectAssignedSupportRequest request) {

                supportRequestService.teamReject(itemId, request);

                return ApiResponse.<Void>builder().build();
        }

        // Danh sách các team trong tỉnh + vị trí SOS
        @GetMapping("/{id}/candidate-teams")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<SupportMapResponse> getCandidateTeams(
                        @PathVariable UUID id) {

                return ApiResponse
                                .<SupportMapResponse>builder()
                                .result(
                                                supportRequestService.getCandidateTeams(id))
                                .build();
        }

        // Province xem danh sách Support Item theo trạng thái
        @GetMapping("/items/status")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Page<ProvinceSupportItemResponse>> getSupportItemsByStatus(
                        @RequestParam SupportRequestItemStatus status,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<ProvinceSupportItemResponse>>builder()
                                .result(
                                                supportRequestService.getSupportItemsByStatus(
                                                                status,
                                                                pageable))
                                .build();
        }
}
