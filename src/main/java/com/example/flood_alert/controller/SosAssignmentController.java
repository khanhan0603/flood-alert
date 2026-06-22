package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.AssignGroupRequest;
import com.example.flood_alert.dbo.request.UpdateAssignmentStatusRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
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
    public ApiResponse<UUID> assignGroup(
            @RequestBody AssignGroupRequest request) {

        return ApiResponse.<UUID>builder()
                .result(sosAssignmentService.assignGroup(request))
                .build();
    }

    //Group leader cập nhật trạng thái nhiệm vụ
    @PatchMapping("/{assignmentId}/status")
    @PreAuthorize("hasRole('RESCUER')")
    public ApiResponse<Void> updateStatus(@PathVariable UUID assignmentId, @RequestBody @Valid UpdateAssignmentStatusRequest request){
        sosAssignmentService.updateStatus(assignmentId, request);
        return ApiResponse.<Void>builder()
                    .build();
    }
}
