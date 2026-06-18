package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.request.AddGroupMembersRequest;
import com.example.flood_alert.dbo.request.AssignGroupLeaderRequest;
import com.example.flood_alert.dbo.request.CreateRescueGroupRequest;
import com.example.flood_alert.dbo.response.AvailableMemberResponse;
import com.example.flood_alert.dbo.response.GroupLeaderResponse;
import com.example.flood_alert.dbo.response.GroupMemberResponse;
import com.example.flood_alert.dbo.response.ListMemberOfGroupResponse;
import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueGroupMember;
import com.example.flood_alert.entity.RescueGroupMemberId;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RescueGroupService {
        RescueTeamRepository rescueTeamRepository;
        RescueGroupRepository rescueGroupRepository;
        RescueGroupMemberRepository rescueGroupMemberRepository;
        UserRepository userRepository;

        public RescueGroupResponse create(UUID teamId, CreateRescueGroupRequest request) {

                RescueTeam team = rescueTeamRepository.findById(teamId)
                                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

                if (rescueGroupRepository
                                .existsByTeamIdAndName(
                                                teamId,
                                                request.getName())) {

                        throw new AppException(ErrorCode.RESCUE_GROUP_EXISTED);
                }

                RescueGroup group = RescueGroup.builder()
                                .team(team)
                                .name(request.getName())
                                .status(RescueGroupStatus.AVAILABLE)
                                .hasBoat(Boolean.TRUE.equals(request.getHasBoat()))
                                .hasMedical(Boolean.TRUE.equals(request.getHasMedical()))
                                .notes(request.getNotes())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                group = rescueGroupRepository.save(group);

                return RescueGroupResponse.builder()
                                .id(group.getId())
                                .name(group.getName())
                                .teamId(team.getId())
                                .teamName(team.getName())
                                .status(group.getStatus())
                                .hasBoat(group.isHasBoat())
                                .hasMedical(group.isHasMedical())
                                .notes(group.getNotes())
                                .build();
        }

        public List<AvailableMemberResponse> getAvailableMembers(
                        UUID teamId) {

                rescueTeamRepository.findById(teamId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_TEAM_NOT_FOUND));

                return userRepository.findAvailableMembers(teamId)
                                .stream()
                                .map(user -> AvailableMemberResponse.builder()
                                                .userId(user.getId())
                                                .fullName(user.getHoten())
                                                .phone(user.getSodt())
                                                .build())
                                .toList();
        }

        // Add nhóm lực lượng vào group
        public List<GroupMemberResponse> addMembers(
                        UUID groupId,
                        AddGroupMembersRequest request) {

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                List<GroupMemberResponse> responses = new ArrayList<>();

                for (UUID userId : request.getUserIds()) {

                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new AppException(
                                                        ErrorCode.USER_NOT_EXISTED));

                        if (!user.getTeam().getId()
                                        .equals(group.getTeam().getId())) {

                                throw new RuntimeException(
                                                "Người cứu hộ không thuộc đội này");
                        }

                        if (user.getRole() != Role.RESCUER) {

                                throw new RuntimeException(
                                                "Chỉ được thêm RESCUER");
                        }

                        if (rescueGroupMemberRepository
                                        .existsByUser_Id(userId)) {

                                throw new RuntimeException(
                                                "Rescuer đã thuộc group khác");
                        }

                        RescueGroupMember member = RescueGroupMember
                                        .builder()
                                        .id(new RescueGroupMemberId(
                                                        group.getId(),
                                                        user.getId()))
                                        .group(group)
                                        .user(user)
                                        .joinedAt(LocalDateTime.now())
                                        .build();

                        rescueGroupMemberRepository.save(member);

                        responses.add(
                                        GroupMemberResponse.builder()
                                                        .userId(user.getId())
                                                        .fullName(user.getHoten())
                                                        .phone(user.getSodt())
                                                        .build());
                }

                return responses;
        }

        // Pick group leader
        public GroupLeaderResponse assignLeader(
                        UUID groupId,
                        AssignGroupLeaderRequest request) {

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.USER_NOT_EXISTED));

                boolean isMember = rescueGroupMemberRepository
                                .existsByGroup_IdAndUser_Id(
                                                groupId,
                                                user.getId());

                if (!isMember) {
                        throw new RuntimeException(
                                        "Người cứu hộ không thuộc nhóm này");
                }

                if (user.getRole() != Role.RESCUER) {
                        throw new RuntimeException(
                                        "Chỉ được chọn RESCUER");
                }

                group.setLeader(user);

                rescueGroupRepository.save(group);

                return GroupLeaderResponse.builder()
                                .groupId(group.getId())
                                .groupName(group.getName())
                                .leaderId(user.getId())
                                .leaderName(user.getHoten())
                                .phone(user.getSodt())
                                .build();
        }

        // Danh sách thành viên trong group
        public Page<ListMemberOfGroupResponse> getMembers(
                        UUID groupId,
                        Pageable pageable) {

                rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                return rescueGroupMemberRepository
                                .findMembersByGroupId(groupId, pageable);
        }
}
