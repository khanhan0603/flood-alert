package com.example.flood_alert.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDataInitializerService {

    UserRepository userRepository;

    AreaRepository areaRepository;

    PasswordEncoder passwordEncoder;

    public void init() {

        // =========================================
        // CHECK EXIST
        // =========================================

        boolean adminExists =
            userRepository.existsByRole(Role.ADMIN);

        boolean rescuerExists =
            userRepository.existsByRole(Role.RESCUER);

        if (adminExists && rescuerExists) {

            log.info(
                "ADMIN & RESCUER ALREADY EXISTS"
            );

            return;
        }

        // =========================================
        // FIND AREA
        // =========================================

        Area adminArea =
            areaRepository
                .findByTenkhuvuc("Khánh Hòa")
                .orElse(null);

        Area rescuerArea =
            areaRepository
                .findByTenkhuvuc("TP. Hồ Chí Minh")
                .orElse(null);

        // =========================================
        // CREATE ADMIN
        // =========================================

        if (!adminExists) {

            User admin = User.builder()
                .hoten("Nguyễn Tí")
                .gioitinh(true)
                .ngaysinh(
                    LocalDate.of(1995, 1, 1)
                )
                .sodt("0123456789")
                .diachi("Tỉnh Khánh Hòa")
                .email("ti@gmail.com")
                .password(
                    passwordEncoder.encode(
                        "123456"
                    )
                )
                .role(Role.ADMIN)
                .trangthai(Status.ACTIVE)
                .area(adminArea)
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

            userRepository.save(admin);

            log.warn(
                "DEFAULT ADMIN CREATED"
            );
        }

        // =========================================
        // CREATE RESCUER
        // =========================================

        if (!rescuerExists) {

            User rescuer = User.builder()
                .hoten("Thị Sửu")
                .gioitinh(false)
                .ngaysinh(
                    LocalDate.of(2000, 1, 1)
                )
                .sodt("0976584321")
                .diachi("TP. Hồ Chí Minh")
                .email("suu@gmail.com")
                .password(
                    passwordEncoder.encode(
                        "123456"
                    )
                )
                .role(Role.RESCUER)
                .trangthai(Status.ACTIVE)
                .area(rescuerArea)
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

            userRepository.save(rescuer);

            log.warn(
                "DEFAULT RESCUER CREATED"
            );
        }

        log.warn(
            "DEFAULT PASSWORD: 123456"
        );
    }
}

