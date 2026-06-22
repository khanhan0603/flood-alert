package com.example.flood_alert.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.ProvinceOperatorExcelRow;
import com.example.flood_alert.dbo.response.ImportProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.RowError;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
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
public class ProvinceOperatorImportService {
    ProvinceOperatorExcelReader provinceOperatorExcelReader;
    UserRepository userRepository;
    AreaRepository areaRepository;
    PasswordEncoder passwordEncoder;

    public ImportProvinceOperatorResponse importExcel(MultipartFile file)
            throws IOException {
        List<ProvinceOperatorExcelRow> rows = provinceOperatorExcelReader.read(file);

        log.info("Rows size={}", rows.size());

        List<RowError> errors = new ArrayList<>();
        List<User> users = new ArrayList<>();

        Set<String> existingEmails = userRepository.findAllEmails()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> existingPhones = new HashSet<>(userRepository.findAllPhones());

        Map<String, Area> provinceMap = areaRepository.findByLevel(1)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getTenkhuvuc().trim().toLowerCase(),
                        Function.identity()));

        for (ProvinceOperatorExcelRow row : rows) {
            if (isBlank(row.getEmail())) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Email trống")
                                .build());

                continue;
            }
            if (isBlank(row.getSodt())) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Số điện thoại trống")
                                .build());

                continue;
            }
            String email = row.getEmail().trim().toLowerCase();

            String sodt = row.getSodt().trim();

            if (isBlank(row.getHoten())) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Họ tên trống")
                                .build());

                continue;
            }

            if (row.getNgaysinh() == null) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Ngày sinh không hợp lệ")
                                .build());

                continue;
            }

            if (row.getGioitinh() == null) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Giới tính không hợp lệ")
                                .build());

                continue;
            }

            if (!isValidEmail(email)) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Email không hợp lệ")
                                .build());

                continue;
            }

            if (!isValidPhone(sodt)) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Số điện thoại không hợp lệ")
                                .build());

                continue;
            }

            if (existingEmails.contains(email)) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Email đã tồn tại")
                                .build());

                continue;
            }

            if (existingPhones.contains(sodt)) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message("Số điện thoại đã tồn tại")
                                .build());

                continue;
            }

            Area province = provinceMap.get(
                    row.getProvinceName()
                            .trim()
                            .toLowerCase());

            if (province == null) {

                errors.add(
                        RowError.builder()
                                .row(row.getRowNumber())
                                .message(
                                        "Không tìm thấy tỉnh: "
                                                + row.getProvinceName())
                                .build());

                continue;
            }

            User user = User.builder()
                    .hoten(row.getHoten())
                    .gioitinh(row.getGioitinh())
                    .ngaysinh(row.getNgaysinh())
                    .sodt(sodt)
                    .email(email)
                    .password(
                            passwordEncoder.encode("123456"))
                    .role(Role.PROVINCE_OPERATOR)
                    .trangthai(Status.ACTIVE)
                    .diachi(row.getDiachi())
                    .area(province)
                    .team(null)
                    .build();

            users.add(user);

            existingEmails.add(email);
            existingPhones.add(sodt);

        }
        userRepository.saveAll(users);

        return ImportProvinceOperatorResponse
                .builder()
                .successCount(users.size())
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^0\\d{9}$");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
