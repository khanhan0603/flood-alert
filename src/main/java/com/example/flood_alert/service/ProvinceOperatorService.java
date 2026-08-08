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
import com.example.flood_alert.dbo.request.UpdateProvinceOperatorRequest;
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
                                .gioitinh(user.isGioitinh())
                                .ngaysinh(user.getNgaysinh())
                                .sodt(user.getSodt())
                                .diachi(user.getDiachi())
                                .email(user.getEmail())
                                .ghichu(user.getGhichu())
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

                if (userRepository.existsActiveByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.EMAIL_EXISTED);
                }

                if (userRepository.existsActiveBySodt(request.getSodt())) {
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

        @Transactional
        public ProvinceOperatorDetailResponse update(UpdateProvinceOperatorRequest request) {
                User province = userRepository.findById(request.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                if (request.getEmail() != null
                                && !request.getEmail().equals(province.getEmail())
                                && userRepository.existsActiveByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.EMAIL_EXISTED);
                }
                if (request.getSodt() != null
                                && !request.getSodt().equals(province.getSodt())
                                && userRepository.existsActiveBySodt(request.getSodt())) {
                        throw new AppException(ErrorCode.PHONE_EXISTED);
                }
                if (request.getHoten() != null)
                        province.setHoten(request.getHoten());

                if (request.getGioitinh() != null)
                        province.setGioitinh(request.getGioitinh());

                if (request.getNgaysinh() != null)
                        province.setNgaysinh(request.getNgaysinh());

                if (request.getSodt() != null)
                        province.setSodt(request.getSodt());

                if (request.getDiachi() != null)
                        province.setDiachi(request.getDiachi());

                if (request.getEmail() != null)
                        province.setEmail(request.getEmail());

                if (request.getGhichu() != null)
                        province.setGhichu(request.getGhichu());
                        
                province.setArea(areaRepository.findById(request.getAreaId())
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND)));
                userRepository.save(province);

                return ProvinceOperatorDetailResponse
                                .builder()
                                .id(province.getId())
                                .hoten(province.getHoten())
                                .gioitinh(province.isGioitinh())
                                .ngaysinh(province.getNgaysinh())
                                .sodt(province.getSodt())
                                .diachi(province.getDiachi())
                                .email(province.getEmail())
                                .ghichu(province.getGhichu())
                                .areaId(province.getArea().getId())
                                .tenKhuVucPhuTrach(
                                                province.getArea()
                                                                .getTenkhuvuc())
                                .teamCount(rescueTeamRepository
                                                .countByProvinceId(
                                                                province.getArea()
                                                                                .getId()))
                                .build();
        }
}
