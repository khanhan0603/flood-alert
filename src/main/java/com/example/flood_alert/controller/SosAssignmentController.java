package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.AssignGroupRequest;
import com.example.flood_alert.dbo.request.AssignSupportGroupRequest;
import com.example.flood_alert.dbo.request.FailAssignmentRequest;
import com.example.flood_alert.dbo.request.UpdateAssignmentStatusRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.AssignCandidateGroupResponse;
import com.example.flood_alert.dbo.response.AssignmentResponse;
import com.example.flood_alert.dbo.response.AssignmentStatusOptionResponse;
import com.example.flood_alert.dbo.response.GroupAssignmentResponse;
import com.example.flood_alert.service.SosAssignmentService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sos-assignment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SosAssignmentController {
        SosAssignmentService sosAssignmentService;

        // Giao nhiệm vụ cho group
        @PostMapping
        public ApiResponse<AssignmentResponse> assignGroup(
                        @RequestBody AssignGroupRequest request) {

                return ApiResponse.<AssignmentResponse>builder()
                                .result(sosAssignmentService.assignGroup(request))
                                .build();
        }

        // Group leader xem các trạng thái nhiệm vụ
        @GetMapping("/{id}/available-statuses")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<List<AssignmentStatusOptionResponse>> getAvailableStatuses(
                        @PathVariable UUID id) {

                return ApiResponse
                                .<List<AssignmentStatusOptionResponse>>builder()
                                .result(sosAssignmentService.getAvailableStatuses(id))
                                .build();
        }

        // Group leader cập nhật trạng thái nhiệm vụ
        @PatchMapping("/{assignmentId}/status")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Void> updateStatus(@PathVariable UUID assignmentId,
                        @RequestBody @Valid UpdateAssignmentStatusRequest request) {
                sosAssignmentService.updateStatus(assignmentId, request);
                return ApiResponse.<Void>builder()
                                .build();
        }

        // Danh sách nhiệm vụ của group
        @GetMapping("/my-group")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<List<GroupAssignmentResponse>> getMyAssignments() {

                return ApiResponse
                                .<List<GroupAssignmentResponse>>builder()
                                .result(
                                                sosAssignmentService
                                                                .getMyAssignments())
                                .build();
        }

        // Group leader báo fail
        @PatchMapping("/{assignmentId}/failed")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Void> failed(
                        @PathVariable UUID assignmentId,
                        @Valid @RequestBody FailAssignmentRequest request) {

                sosAssignmentService.failed(
                                assignmentId,
                                request);

                return ApiResponse.<Void>builder()
                                .build();
        }

        // Danh sách Group có thể giao thực hiện SOS
        @GetMapping("/assign-candidates/{sosId}")
        public ApiResponse<List<AssignCandidateGroupResponse>> getAssignCandidateGroups(
                        @PathVariable UUID sosId) {

                return ApiResponse.<List<AssignCandidateGroupResponse>>builder()
                                .result(sosAssignmentService.getAssignCandidateGroups(sosId))
                                .build();
        }
}
