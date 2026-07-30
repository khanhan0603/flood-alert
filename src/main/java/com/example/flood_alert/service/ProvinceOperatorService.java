package com.example.flood_alert.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.CreateProvinceOperatorRequest;
import com.example.flood_alert.dbo.request.DeleteProvinceOperatorRequest;
import com.example.flood_alert.dbo.response.ProvinceOperatorDetailResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.RescueTeamSummaryResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
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
public class ProvinceOperatorService {
        UserRepository userRepository;
        RescueTeamRepository rescueTeamRepository;
        RescueGroupRepository rescueGroupRepository;
        AreaRepository areaRepository;
        PasswordEncoder passwordEncoder;

        // Danh sách các province
        @Transactional(readOnly = true)
        public Page<ProvinceOperatorResponse> getAll(Pageable pageable) {

                return userRepository
                                .findByRole(
                                                Role.PROVINCE_OPERATOR,
                                                pageable)
                                .map(user -> ProvinceOperatorResponse
                                                .builder()
                                                .id(user.getId())
                                                .hoten(user.getHoten())
                                                .tenkhuvuc_phutrach(
                                                                user.getArea()
                                                                                .getTenkhuvuc())
                                                .build());
        }

        // Chi tiết province
        @Transactional(readOnly = true)
        public ProvinceOperatorDetailResponse getDetail(
                        UUID id) {

                User user = userRepository
                                .findById(id)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.USER_NOT_EXISTED));

                long teamCount = rescueTeamRepository
                                .countByProvinceId(
                                                user.getArea().getId());

                return ProvinceOperatorDetailResponse
                                .builder()
                                .id(user.getId())
                                .hoten(user.getHoten())
                                .sodt(user.getSodt())
                                .email(user.getEmail())
                                .areaId(user.getArea().getId())
                                .tenKhuVucPhuTrach(
                                                user.getArea()
                                                                .getTenkhuvuc())
                                .teamCount(teamCount)
                                .build();
        }

        @Transactional(readOnly = true)
        public Page<RescueTeamSummaryResponse> getTeams(
                        UUID provinceOperatorId,
                        Pageable pageable) {

                User provinceOperator = userRepository
                                .findById(provinceOperatorId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.USER_NOT_EXISTED));

                UUID provinceId = provinceOperator.getArea().getId();

                return rescueTeamRepository
                                .findByProvinceId(
                                                provinceId,
                                                pageable)
                                .map(team -> RescueTeamSummaryResponse
                                                .builder()
                                                .id(team.getId())
                                                .name(team.getName())
                                                .leaderName(
                                                                team.getLeader() != null
                                                                                ? team.getLeader()
                                                                                                .getHoten()
                                                                                : null)
                                                .groupCount(
                                                                rescueGroupRepository
                                                                                .countByTeamId(
                                                                                                team.getId()))
                                                .build());
        }

        @Transactional
        public ProvinceOperatorResponse create(
                        CreateProvinceOperatorRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.EMAIL_EXISTED);
                }

                if (userRepository.existsBySodt(request.getSodt())) {
                        throw new AppException(ErrorCode.PHONE_EXISTED);
                }

                Area area = areaRepository.findById(request.getAreaId())
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

                User provinceOperator = User.builder()
                                .hoten(request.getHoten())
                                .email(request.getEmail())
                                .sodt(request.getSodt())
                                .gioitinh(request.getGioitinh())
                                .ngaysinh(request.getNgaysinh())
                                .diachi(request.getDiachi())
                                .password(passwordEncoder.encode("123456"))
                                .role(Role.PROVINCE_OPERATOR)
                                .trangthai(Status.ACTIVE)
                                .area(area)
                                .team(null)
                                .build();

                provinceOperator = userRepository.save(provinceOperator);

                return ProvinceOperatorResponse.builder()
                                .id(provinceOperator.getId())
                                .hoten(provinceOperator.getHoten())
                                .tenkhuvuc_phutrach(
                                                provinceOperator.getArea().getTenkhuvuc())
                                .build();
        }

        @Transactional
        public void delete(DeleteProvinceOperatorRequest request) {

                List<User> provinceOperators = userRepository.findAllByIdIn(request.getIds());

                if (provinceOperators.size() != request.getIds().size()) {
                        throw new AppException(ErrorCode.USER_NOT_EXISTED);
                }

                for (User user : provinceOperators) {
                        if (user.getRole() != Role.PROVINCE_OPERATOR) {
                                throw new AppException(ErrorCode.USER_IS_NOT_PROVINCE_OPERATOR);
                        }
                        user.setTrangthai(Status.INACTIVE);
                }

                userRepository.saveAll(provinceOperators);
        }
}
