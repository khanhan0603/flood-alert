package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.UserCreationRequest;
import com.example.flood_alert.dbo.response.ProvinceOperatorResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.UserMapper;
import com.example.flood_alert.repository.AreaRepository;
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
    UserMapper userMapper;

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

    // Danh sách các province
    @Transactional(readOnly = true)
    public Page<ProvinceOperatorResponse> getAll(
            Pageable pageable) {

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
}
