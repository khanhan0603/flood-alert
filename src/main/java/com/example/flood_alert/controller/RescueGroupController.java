package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.AddGroupMembersRequest;
import com.example.flood_alert.dbo.request.AssignGroupLeaderRequest;
import com.example.flood_alert.dbo.request.CreateRescueGroupRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.AvailableMemberResponse;
import com.example.flood_alert.dbo.response.GroupLeaderResponse;
import com.example.flood_alert.dbo.response.GroupMemberResponse;
import com.example.flood_alert.dbo.response.ListMemberOfGroupResponse;
import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.service.RescueGroupService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/res-groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RescueGroupController {
        RescueGroupService rescueGroupService;

        @PostMapping("/team/{teamId}")
        public ApiResponse<RescueGroupResponse> create(@PathVariable UUID teamId,
                        @RequestBody CreateRescueGroupRequest request) {
                return ApiResponse.<RescueGroupResponse>builder()
                                .result(
                                                rescueGroupService.create(
                                                                teamId,
                                                                request))
                                .build();
        }

        @GetMapping("/team/{teamId}/available-members")
        public ApiResponse<List<AvailableMemberResponse>> getAvailableMembers(
                        @PathVariable UUID teamId) {

                return ApiResponse
                                .<List<AvailableMemberResponse>>builder()
                                .result(
                                                rescueGroupService
                                                                .getAvailableMembers(teamId))
                                .build();
        }

        @PutMapping("/{groupId}/members")
        public ApiResponse<List<GroupMemberResponse>> addMembers(
                        @PathVariable UUID groupId,
                        @RequestBody AddGroupMembersRequest request) {

                return ApiResponse
                                .<List<GroupMemberResponse>>builder()
                                .result(
                                                rescueGroupService.addMembers(
                                                                groupId,
                                                                request))
                                .build();
        }

        // Pick group leader
        @PutMapping("/{groupId}/leader")
        public ApiResponse<GroupLeaderResponse> assignLeader(
                        @PathVariable UUID groupId,
                        @RequestBody AssignGroupLeaderRequest request) {

                return ApiResponse
                                .<GroupLeaderResponse>builder()
                                .result(
                                                rescueGroupService.assignLeader(
                                                                groupId,
                                                                request))
                                .build();
        }

        // Danh sách các thành viên trong group
        @GetMapping("/{groupId}/members")
        public ApiResponse<Page<ListMemberOfGroupResponse>> getMembers(
                        @PathVariable UUID groupId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ApiResponse.<Page<ListMemberOfGroupResponse>>builder()
                                .result(rescueGroupService.getMembers(groupId, pageable))
                                .build();
        }

        // Loại thành viên ra khỏi nhóm, team leader mới được làm
        @DeleteMapping("/{groupId}/members/{userId}")
        public ApiResponse<Void> removeMember(
                        @PathVariable UUID groupId,
                        @PathVariable UUID userId) {

                rescueGroupService.removeMember(groupId, userId);

                return ApiResponse.<Void>builder().build();
        }
}
