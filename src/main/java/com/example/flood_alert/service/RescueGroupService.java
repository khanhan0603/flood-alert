package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.AddGroupMembersRequest;
import com.example.flood_alert.dbo.request.AssignGroupLeaderRequest;
import com.example.flood_alert.dbo.request.CreateRescueGroupRequest;
import com.example.flood_alert.dbo.request.UpdateRescueGroupRequest;
import com.example.flood_alert.dbo.request.UpdateRescueGroupStatusRequest;
import com.example.flood_alert.dbo.response.AvailableMemberResponse;
import com.example.flood_alert.dbo.response.GroupDetailResponse;
import com.example.flood_alert.dbo.response.GroupLeaderResponse;
import com.example.flood_alert.dbo.response.GroupMemberResponse;
import com.example.flood_alert.dbo.response.ListMemberOfGroupResponse;
import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.dbo.response.SupportCandidateGroupResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueGroupMember;
import com.example.flood_alert.entity.RescueGroupMemberId;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SupportRequestItem;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SupportRequestItemRepository;
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
        AuthenticationService authenticationService;
        SupportRequestItemRepository supportRequestItemRepository;

        private static final int BOAT_MIN = 3;
        private static final int BOAT_MAX = 5;

        private static final int SEARCH_RESCUE_MIN = 10;
        private static final int SEARCH_RESCUE_MAX = 25;

        private static final int LOGISTICS_MIN = 5;
        private static final int LOGISTICS_MAX = 8;

        public RescueGroupResponse create(
                        UUID teamId,
                        CreateRescueGroupRequest request) {

                RescueTeam team = rescueTeamRepository.findById(teamId)
                                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

                if (rescueGroupRepository.existsByTeam_IdAndNameAndStatus(
                                teamId,
                                request.getName(),
                                RescueGroupStatus.AVAILABLE)) {

                        throw new AppException(ErrorCode.RESCUE_GROUP_EXISTED);
                }

                // Năng lực của nhóm
                boolean hasSearchRescue = Boolean.TRUE.equals(request.getHasSearchRescue());
                boolean hasLogistics = Boolean.TRUE.equals(request.getHasLogistics());

                // Search & Rescue và Logistics mặc định có xuồng + y tế
                boolean hasBoat = Boolean.TRUE.equals(request.getHasBoat())
                                || hasSearchRescue
                                || hasLogistics;

                boolean hasMedical = Boolean.TRUE.equals(request.getHasMedical())
                                || hasSearchRescue
                                || hasLogistics;

                RescueGroup group = RescueGroup.builder()
                                .team(team)
                                .name(request.getName())
                                .status(RescueGroupStatus.AVAILABLE)
                                .type(RescueGroupType.OPERATIONAL)
                                .hasBoat(hasBoat)
                                .hasMedical(hasMedical)
                                .hasSearchRescue(hasSearchRescue)
                                .hasLogistics(hasLogistics)

                                .notes(request.getNotes())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                // Check xem có tạo nhầm nhóm hotline với năng lực ko đc phép ko
                validateHotlineGroup(group);

                group = rescueGroupRepository.save(group);

                return RescueGroupResponse.builder()
                                .id(group.getId())
                                .name(group.getName())

                                .teamId(team.getId())
                                .teamName(team.getName())

                                .status(group.getStatus())

                                .hasBoat(group.isHasBoat())
                                .hasMedical(group.isHasMedical())
                                .hasSearchRescue(group.isHasSearchRescue())
                                .hasLogistics(group.isHasLogistics())

                                .notes(group.getNotes())
                                .build();
        }

        private void validateHotlineGroup(RescueGroup group) {

                if (group.getType() == RescueGroupType.HOTLINE) {

                        if (group.isHasBoat()
                                        || group.isHasMedical()
                                        || group.isHasSearchRescue()
                                        || group.isHasLogistics()) {

                                throw new AppException(ErrorCode.INVALID_HOTLINE_GROUP_CAPABILITY);
                        }
                }
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
                                                .email(user.getEmail())
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

                long currentMember = rescueGroupMemberRepository.countByGroup_Id(groupId);

                if (currentMember + request.getUserIds().size() > getMaxMembers(group)) {
                        throw new AppException(ErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED);
                }

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
                                                "Chỉ được thêm lực lượng cứu hộ");
                        }

                        if (rescueGroupMemberRepository
                                        .existsByUser_Id(userId)) {

                                throw new RuntimeException(
                                                "Cứu hộ viên đã thuộc nhóm cứu hộ khác");
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

        private int getMaxMembers(RescueGroup group) {

                if (group.isHasSearchRescue()) {
                        return SEARCH_RESCUE_MAX;
                }

                if (group.isHasLogistics()) {
                        return LOGISTICS_MAX;
                }

                if (group.isHasBoat()) {
                        return BOAT_MAX;
                }

                return Integer.MAX_VALUE;
        }

        // Pick group leader
        @Transactional
        public GroupLeaderResponse assignLeader(
                        UUID groupId,
                        AssignGroupLeaderRequest request) {
                User currentUser = authenticationService.getCurrentUser();

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                RescueTeam team = group.getTeam();

                if (team.getLeader() == null
                                || !team.getLeader().getId().equals(currentUser.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

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

                boolean isLeader = rescueGroupRepository.existsByLeaderId(request.getUserId());

                if (isLeader) {
                        throw new RuntimeException("Người dùng này đã nhận vai trò trưởng nhóm thuộc nhóm khác!");
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

        // Loại thành viên ra khỏi group
        @Transactional
        public void removeMember(UUID groupId, UUID userId) {

                User currentUser = authenticationService.getCurrentUser();

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                RescueTeam team = group.getTeam();

                if (team.getLeader() == null ||
                                !team.getLeader().getId().equals(currentUser.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                RescueGroupMember member = rescueGroupMemberRepository
                                .findByGroup_IdAndUser_Id(groupId, userId)
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_MEMBER_NOT_FOUND));

                // Không cho loại group leader khỏi nhóm
                if (group.getLeader() != null
                                && group.getLeader().getId().equals(userId)) {

                        throw new AppException(
                                        ErrorCode.GROUP_LEADER_CANNOT_REMOVE);
                }

                rescueGroupMemberRepository.delete(member);
        }

        // Cập nhật trạng thái nhóm do team leader và group leader làm
        @Transactional
        public void updateStatus(
                        UUID groupId,
                        UpdateRescueGroupStatusRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                boolean isTeamLeader = group.getTeam().getLeader() != null
                                && group.getTeam().getLeader().getId().equals(currentUser.getId());

                boolean isGroupLeader = group.getLeader() != null
                                && group.getLeader().getId().equals(currentUser.getId());

                if (!isTeamLeader && !isGroupLeader) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                if (request.getStatus() == RescueGroupStatus.BUSY) {
                        throw new AppException(ErrorCode.INVALID_GROUP_STATUS);
                }

                // AVAILABLE -> DISBANDED
                if (group.getStatus() == RescueGroupStatus.AVAILABLE
                                && request.getStatus() == RescueGroupStatus.DISBANDED) {

                        removeAllMembers(group);
                }

                // OFFLINE -> AVAILABLE
                group.setStatus(request.getStatus());

                rescueGroupRepository.save(group);
        }

        // loại toàn bộ thành viên
        private void removeAllMembers(RescueGroup group) {

                // Bỏ trưởng nhóm
                group.setLeader(null);

                // Xóa toàn bộ thành viên
                rescueGroupMemberRepository.deleteAllByGroupId(group.getId());
        }

        // Danh sách các group available theo loại hỗ trợ cần
        @Transactional(readOnly = true)
        public List<SupportCandidateGroupResponse> getSupportCandidateGroups(UUID supportRequestItemId) {

                // Team Leader đang đăng nhập
                User currentUser = authenticationService.getCurrentUser();

                // Team Leader
                RescueTeam myTeam = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.NO_PERMISSION));

                // Tìm hạng mục hỗ trợ
                SupportRequestItem item = supportRequestItemRepository
                                .findById(supportRequestItemId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_ITEM_NOT_FOUND));

                // Chỉ xem yêu cầu hỗ trợ của Team mình
                if (!item.getSupportRequest()
                                .getRequestedBy()
                                .getTeam()
                                .getId()
                                .equals(myTeam.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Lấy các Group phù hợp với loại hỗ trợ
                List<RescueGroup> groups;

                switch (item.getSupportType()) {

                        case BOAT ->
                                groups = rescueGroupRepository.findByTeam_IdAndStatusAndHasBoatTrue(
                                                myTeam.getId(),
                                                RescueGroupStatus.AVAILABLE);

                        case MEDICAL ->
                                groups = rescueGroupRepository.findByTeam_IdAndStatusAndHasMedicalTrue(
                                                myTeam.getId(),
                                                RescueGroupStatus.AVAILABLE);

                        case SEARCH_RESCUE ->
                                groups = rescueGroupRepository.findByTeam_IdAndStatusAndHasSearchRescueTrue(
                                                myTeam.getId(),
                                                RescueGroupStatus.AVAILABLE);

                        case LOGISTICS ->
                                groups = rescueGroupRepository.findByTeam_IdAndStatusAndHasLogisticsTrue(
                                                myTeam.getId(),
                                                RescueGroupStatus.AVAILABLE);

                        default ->
                                throw new AppException(ErrorCode.INVALID_SUPPORT_TYPE);
                }

                return groups.stream()
                                .map(group -> {

                                        SupportCandidateGroupResponse response = new SupportCandidateGroupResponse();

                                        response.setId(group.getId());
                                        response.setGroupName(group.getName());

                                        response.setLeaderName(
                                                        group.getLeader() != null
                                                                        ? group.getLeader().getHoten()
                                                                        : null);

                                        response.setStatus(group.getStatus());

                                        response.setHasBoat(group.isHasBoat());
                                        response.setHasMedical(group.isHasMedical());
                                        response.setHasSearchRescue(group.isHasSearchRescue());
                                        response.setHasLogistics(group.isHasLogistics());

                                        return response;
                                })
                                .toList();
        }

        // Chi tiết thông tin nhóm
        @Transactional(readOnly = true)
        public GroupDetailResponse getDetail(UUID groupId) {

                RescueGroup group = rescueGroupRepository.findDetailById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                // Danh sách member
                List<GroupMemberResponse> members = rescueGroupMemberRepository
                                .findAllWithUserByGroupId(groupId)
                                .stream()
                                .map(member -> GroupMemberResponse.builder()
                                                .userId(member.getUser().getId())
                                                .fullName(member.getUser().getHoten())
                                                .phone(member.getUser().getSodt())
                                                .build())
                                .toList();

                // Số lượng thành viên trong nhóm
                long currentMember = rescueGroupMemberRepository.countByGroup_Id(groupId);

                GroupLeaderResponse leader = null;

                if (group.getLeader() != null) {

                        leader = GroupLeaderResponse.builder()
                                        .groupId(group.getId())
                                        .groupName(group.getName())
                                        .leaderId(group.getLeader().getId())
                                        .leaderName(group.getLeader().getHoten())
                                        .phone(group.getLeader().getSodt())
                                        .build();
                }

                return GroupDetailResponse.builder()
                                .id(group.getId())
                                .name(group.getName())
                                .status(group.getStatus())
                                .type(group.getType())

                                .teamId(group.getTeam().getId())
                                .teamName(group.getTeam().getName())

                                .hasBoat(group.isHasBoat())
                                .hasMedical(group.isHasMedical())
                                .hasSearchRescue(group.isHasSearchRescue())
                                .hasLogistics(group.isHasLogistics())

                                .currentMember(currentMember)
                                .minMember(getMinMembers(group))
                                .maxMember(getMaxMembers(group))
                                .enoughMember(currentMember >= getMinMembers(group))

                                .notes(group.getNotes())

                                .leader(leader)
                                .members(members)
                                .build();
        }

        private int getMinMembers(RescueGroup group) {

                if (group.isHasSearchRescue()) {
                        return SEARCH_RESCUE_MIN;
                }

                if (group.isHasLogistics()) {
                        return LOGISTICS_MIN;
                }

                if (group.isHasBoat()) {
                        return BOAT_MIN;
                }

                return 1;
        }

        // Cập nhật thông tin nhóm
        @Transactional
        public RescueGroupResponse updateGroup(
                        UUID groupId,
                        UpdateRescueGroupRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                RescueGroup group = rescueGroupRepository.findById(groupId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                RescueTeam team = group.getTeam();

                if (team.getLeader() == null
                                || !team.getLeader().getId().equals(currentUser.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                if (group.getStatus() == RescueGroupStatus.BUSY) {
                        throw new AppException(ErrorCode.INVALID_GROUP_STATUS);
                }

                if (!group.getName().equals(request.getName())
                                && rescueGroupRepository.existsByTeam_IdAndNameAndIdNot(
                                                team.getId(),
                                                request.getName(),
                                                groupId)) {

                        throw new AppException(ErrorCode.RESCUE_GROUP_EXISTED);
                }

                boolean hasSearchRescue = Boolean.TRUE.equals(request.getHasSearchRescue());
                boolean hasLogistics = Boolean.TRUE.equals(request.getHasLogistics());

                boolean hasBoat = Boolean.TRUE.equals(request.getHasBoat())
                                || hasSearchRescue
                                || hasLogistics;

                boolean hasMedical = Boolean.TRUE.equals(request.getHasMedical())
                                || hasSearchRescue
                                || hasLogistics;

                group.setName(request.getName());

                group.setHasBoat(hasBoat);
                group.setHasMedical(hasMedical);
                group.setHasSearchRescue(hasSearchRescue);
                group.setHasLogistics(hasLogistics);

                group.setNotes(request.getNotes());

                group.setUpdatedAt(LocalDateTime.now());

                validateHotlineGroup(group);

                rescueGroupRepository.save(group);

                return RescueGroupResponse.builder()
                                .id(group.getId())
                                .name(group.getName())

                                .teamId(team.getId())
                                .teamName(team.getName())

                                .status(group.getStatus())

                                .hasBoat(group.isHasBoat())
                                .hasMedical(group.isHasMedical())
                                .hasSearchRescue(group.isHasSearchRescue())
                                .hasLogistics(group.isHasLogistics())

                                .notes(group.getNotes())
                                .build();
        }

        @Transactional(readOnly = true)
        public Page<RescueGroupResponse> getDisbandedGroups(Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                return rescueGroupRepository
                                .findByTeam_IdAndStatus(
                                                team.getId(),
                                                RescueGroupStatus.DISBANDED,
                                                pageable)
                                .map(group -> RescueGroupResponse.builder()
                                                .id(group.getId())
                                                .name(group.getName())

                                                .teamId(team.getId())
                                                .teamName(team.getName())

                                                .status(group.getStatus())

                                                .hasBoat(group.isHasBoat())
                                                .hasMedical(group.isHasMedical())
                                                .hasSearchRescue(group.isHasSearchRescue())
                                                .hasLogistics(group.isHasLogistics())

                                                .notes(group.getNotes())
                                                .build());
        }
}
