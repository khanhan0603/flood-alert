package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.UpdateUserRequest;
import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.MyProfileResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.UserMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AreaRepository areaRepository;
    RescueGroupMemberRepository rescueGroupMemberRepository;
    UserMapper userMapper;
    AuthenticationService authenticationService;

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        if (userRepository.existsBySodt(request.getSodt()))
            throw new AppException(ErrorCode.PHONE_EXISTED);
        User user = userMapper.toUser(request);

        UUID areaId = UUID.fromString(request.getArea_id());

        Area area = areaRepository.findById(areaId).orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

        user.setArea(area);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CITIZEN);
        user.setTrangthai(Status.ACTIVE);
        user.setCreated_at(LocalDateTime.now());
        user.setUpdated_at(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public MyProfileResponse updateMyProfile(UpdateUserRequest request) {

        User user = authenticationService.getCurrentUser();

        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if (request.getSodt() != null
                && !request.getSodt().equals(user.getSodt())
                && userRepository.existsBySodt(request.getSodt())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        if (request.getHoten() != null)
            user.setHoten(request.getHoten());

        if (request.getGioitinh() != null)
            user.setGioitinh(request.getGioitinh());

        if (request.getNgaysinh() != null)
            user.setNgaysinh(request.getNgaysinh());

        if (request.getSodt() != null)
            user.setSodt(request.getSodt());

        if (request.getDiachi() != null)
            user.setDiachi(request.getDiachi());

        if (request.getEmail() != null)
            user.setEmail(request.getEmail());

        if (request.getGhichu() != null)
            user.setGhichu(request.getGhichu());

        // Chỉ công dân được phép thay đổi khu vực sinh sống
        if (user.getRole() == Role.CITIZEN
                && request.getAreaId() != null) {

            Area area = areaRepository.findById(UUID.fromString(request.getAreaId()))
                    .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

            user.setArea(area);
        }

        userRepository.save(user);
        return getMyProfile();
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile() {

        User user = authenticationService.getCurrentUser();

        MyProfileResponse response = MyProfileResponse.builder()
                .id(user.getId())
                .hoten(user.getHoten())
                .gioitinh(user.isGioitinh())
                .ngaysinh(user.getNgaysinh())
                .email(user.getEmail())
                .sodt(user.getSodt())
                .diachi(user.getDiachi())
                .ghichu(user.getGhichu())
                .role(user.getRole())
                .area(user.getArea() != null ? user.getArea().getTenkhuvuc() : null)
                .build();

        switch (user.getRole()) {

            case ADMIN -> {
                response.setChucVu("Quản trị viên");
            }

            case PROVINCE_OPERATOR -> {
                response.setChucVu("Điều hành cấp tỉnh");

                if (user.getArea() != null) {
                    response.setProvince(user.getArea().getTenkhuvuc());
                }
            }

            case CITIZEN -> {
                // Không có thông tin đội, nhóm, chức vụ
            }

            case RESCUER -> {

                rescueGroupMemberRepository.findByUserId(user.getId())
                        .ifPresent(member -> {

                            RescueGroup group = member.getGroup();

                            if (group != null) {

                                response.setRescueGroup(group.getName());

                                if (group.getTeam() != null) {
                                    response.setRescueTeam(group.getTeam().getName());

                                    if (group.getTeam().getLeader() != null
                                            && group.getTeam().getLeader().getId().equals(user.getId())) {

                                        response.setChucVu("Đội trưởng");
                                    }
                                }

                                // Chưa phải đội trưởng thì kiểm tra nhóm trưởng
                                if (response.getChucVu() == null) {

                                    if (group.getLeader() != null
                                            && group.getLeader().getId().equals(user.getId())) {

                                        response.setChucVu("Nhóm trưởng");
                                    } else {
                                        response.setChucVu("Thành viên");
                                    }
                                }
                            }
                        });

                // Chưa tham gia nhóm nhưng đã thuộc đội
                if (response.getRescueTeam() == null && user.getTeam() != null) {

                    response.setRescueTeam(user.getTeam().getName());

                    if (user.getTeam().getLeader() != null
                            && user.getTeam().getLeader().getId().equals(user.getId())) {

                        response.setChucVu("Đội trưởng");
                    } else {
                        response.setChucVu("Thành viên");
                    }
                }
            }
        }

        return response;
    }
}
