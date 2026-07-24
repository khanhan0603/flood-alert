package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.example.flood_alert.dbo.request.CreateGroupSupportRequest;
import com.example.flood_alert.dbo.request.CreateSupportRequest;
import com.example.flood_alert.dbo.request.RejectAssignedSupportRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.CandidateSupportTeamResponse;
import com.example.flood_alert.dbo.response.CreateSupportRequestResponse;
import com.example.flood_alert.dbo.response.GroupSupportRequestDetailResponse;
import com.example.flood_alert.dbo.response.GroupSupportRequestResponse;
import com.example.flood_alert.dbo.response.ProvinceSupportItemResponse;
import com.example.flood_alert.dbo.response.SupportMapResponse;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.dbo.response.TeamSupportRequestResponse;
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
        public ApiResponse<CreateSupportRequestResponse> create(
                        @RequestBody @Valid CreateSupportRequest request) {

                return ApiResponse.<CreateSupportRequestResponse>builder()
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

        // Danh sách các team gợi ý cho province điều phối
        // Province xem danh sách Team phù hợp theo từng hạng mục hỗ trợ
        @GetMapping("/items/{itemId}/candidate-teams")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<List<CandidateSupportTeamResponse>> getCandidateSupportTeams(
                        @PathVariable UUID itemId) {

                return ApiResponse.<List<CandidateSupportTeamResponse>>builder()
                                .result(
                                                supportRequestService.getCandidateSupportTeams(itemId))
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

        // Group Leader gửi yêu cầu hỗ trợ đến Team Leader
        @PostMapping("/group/{assignmentId}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> createGroupSupportRequest(
                        @PathVariable UUID assignmentId,
                        @RequestBody @Valid CreateGroupSupportRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(supportRequestService.createGroupSupportRequest(
                                                assignmentId,
                                                request))
                                .build();
        }

        // Team Leader xem danh sách yêu cầu hỗ trợ từ các Group trong Team
        @GetMapping("/group")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<GroupSupportRequestResponse>> getGroupSupportRequests(
                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                return ApiResponse.<Page<GroupSupportRequestResponse>>builder()
                                .result(supportRequestService.getGroupSupportRequests(pageable))
                                .build();
        }

        // Team Leader xem chi tiết yêu cầu hỗ trợ của Group
        @GetMapping("/group/{supportRequestId}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<GroupSupportRequestDetailResponse> getGroupSupportRequestDetail(
                        @PathVariable UUID supportRequestId) {

                return ApiResponse.<GroupSupportRequestDetailResponse>builder()
                                .result(supportRequestService.getGroupSupportRequestDetail(
                                                supportRequestId))
                                .build();
        }

        // Team Leader giao nhiệm vụ hỗ trợ cho Group trong Team
        @PostMapping("/support-request-items/{supportRequestItemId}/assign-group")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<UUID> assignSupportGroup(
                        @PathVariable UUID supportRequestItemId,
                        @RequestBody @Valid AssignSupportGroupRequest request) {

                return ApiResponse.<UUID>builder()
                                .result(supportRequestService.assignSupportGroup(
                                                supportRequestItemId,
                                                request))
                                .build();
        }

        // Team Leader xem các yêu cầu hỗ trợ do Team mình gửi
        @GetMapping("/my-created")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<TeamSupportRequestResponse>> getMyCreatedSupportRequests(
                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                return ApiResponse.<Page<TeamSupportRequestResponse>>builder()
                                .result(
                                                supportRequestService.getMyCreatedSupportRequests(
                                                                pageable))
                                .build();
        }

        // Province nhận điều phối support
        @PutMapping("/{supportRequestId}/claim-dispatcher")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<Void> claimDispatcher(
                        @PathVariable UUID supportRequestId) {

                supportRequestService.claimDispatcher(supportRequestId);

                return ApiResponse.<Void>builder().build();
        }
}
