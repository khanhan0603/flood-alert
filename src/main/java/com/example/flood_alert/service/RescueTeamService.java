package com.example.flood_alert.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.AssignTeamLeaderRequest;
import com.example.flood_alert.dbo.request.CreateRescueTeamRequest;
import com.example.flood_alert.dbo.request.UpdateRescueTeamRequest;
import com.example.flood_alert.dbo.response.ImportRescuerResponse;
import com.example.flood_alert.dbo.response.RescueGroupResponse;
import com.example.flood_alert.dbo.response.RescueTeamResponse;
import com.example.flood_alert.dbo.response.RowError;
import com.example.flood_alert.dbo.response.TeamLeaderItemResponse;
import com.example.flood_alert.dbo.response.TeamLeaderResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;
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
public class RescueTeamService {
    RescueTeamRepository rescueTeamRepository;
    RescueGroupRepository rescueGroupRepository;
    AreaRepository areaRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationService authenticationService;

    @Transactional
    public RescueTeamResponse create(CreateRescueTeamRequest request) {

        if (rescueTeamRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.RESCUE_TEAM_EXISTED);
        }

        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

        RescueTeam team = RescueTeam.builder()
                .name(request.getName())
                .description(request.getDescription())
                .area(area)
                .lat(request.getLat())
                .lon(request.getLon())
                .emergencyPhone(request.getEmergencyPhone())
                .build();
        team = rescueTeamRepository.save(team);

        // Lưu 1 group hotline khi tạo team
        // Lưu 1 group Hotline khi tạo Team
        RescueGroup hotline = RescueGroup.builder()
                .team(team)
                .name("Hotline")
                .type(RescueGroupType.HOTLINE)
                .status(RescueGroupStatus.AVAILABLE)
                .hasBoat(false)
                .hasMedical(false)
                .hasSearchRescue(false)
                .hasLogistics(false)
                .notes("Nhóm trực Hotline của đội.")
                .build();

        rescueGroupRepository.save(hotline);

        return RescueTeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .areaId(area.getId())
                .areaName(area.getTenkhuvuc())
                .lat(team.getLat())
                .lon(team.getLon())
                .emergencyPhone(team.getEmergencyPhone())
                .build();
    }

    // Import rescue từ excel
    public ImportRescuerResponse importRescuers(UUID teamId, MultipartFile file) {
        Set<String> existingEmails = userRepository.findAllEmails();
        Set<String> existingPhones = userRepository.findAllPhones();
        String defaultPassword = passwordEncoder.encode("123456");
        RescueTeam team = rescueTeamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

        int success = 0;
        int failed = 0;

        List<RowError> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            int headerRowIndex = findHeaderRow(sheet);

            Row headerRow = sheet.getRow(headerRowIndex);

            Map<String, Integer> columns = buildColumnMap(headerRow);
            List<User> usersToSave = new ArrayList<>();
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {

                    String fullName = getStringCell(
                            row.getCell(columns.get("Họ tên")));

                    String genderText = getStringCell(
                            row.getCell(columns.get("Giới tính")));

                    String email = getStringCell(
                            row.getCell(columns.get("Email")));

                    String address = getStringCell(
                            row.getCell(columns.get("Địa chỉ")));

                    String phone = normalizePhone(getStringCell(
                            row.getCell(columns.get("Số điện thoại"))));

                    LocalDate birthDate = getDateCell(
                            row.getCell(columns.get("Ngày sinh")));

                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new RuntimeException("Email không hợp lệ");
                    }

                    if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
                        throw new RuntimeException("Ngày sinh không hợp lệ");
                    }

                    validateRow(fullName, email, phone, existingEmails, existingPhones);

                    existingEmails.add(email);
                    existingPhones.add(phone);

                    boolean gender = "Nam".equalsIgnoreCase(genderText);

                    User user = User.builder()
                            .hoten(fullName)
                            .gioitinh(gender)
                            .email(email)
                            .diachi(address)
                            .sodt(phone)
                            .ngaysinh(birthDate)
                            .role(Role.RESCUER)
                            .trangthai(Status.ACTIVE)
                            .team(team)
                            .password(defaultPassword)
                            .area(team.getArea())
                            .build();

                    usersToSave.add(user);

                    success++;

                } catch (Exception ex) {

                    failed++;

                    errors.add(
                            RowError.builder()
                                    .row(i + 1)
                                    .message(ex.getMessage())
                                    .build());
                }
            }
            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }

        } catch (IOException ex) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }

        return ImportRescuerResponse.builder()
                .success(success)
                .failed(failed)
                .errors(errors)
                .build();
    }

    private String normalizePhone(String phone) {

        if (phone == null) {
            return null;
        }

        phone = phone.replace(".0", "")
                .replaceAll("\\s+", "");

        if (!phone.startsWith("0")) {
            phone = "0" + phone;
        }

        return phone;
    }

    private int findHeaderRow(Sheet sheet) {

        for (Row row : sheet) {

            for (Cell cell : row) {

                String value = getStringCell(cell);

                if ("Họ tên".equalsIgnoreCase(value)) {
                    return row.getRowNum();
                }
            }
        }

        throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
    }

    private Map<String, Integer> buildColumnMap(Row headerRow) {

        Map<String, Integer> columns = new HashMap<>();

        for (Cell cell : headerRow) {

            columns.put(
                    getStringCell(cell),
                    cell.getColumnIndex());
        }

        return columns;
    }

    private void validateRow(
            String fullName,
            String email,
            String phone,
            Set<String> existingEmails,
            Set<String> existingPhones) {

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (existingEmails.contains(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (existingPhones.contains(phone)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
    }

    // Xử lý cả sdt Excel lưu dạng numeric
    private String getStringCell(Cell cell) {

        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {

            case STRING ->
                cell.getStringCellValue().trim();

            case NUMERIC ->
                BigDecimal.valueOf(cell.getNumericCellValue())
                        .toPlainString()
                        .replace(".0", "");

            case BOOLEAN ->
                String.valueOf(cell.getBooleanCellValue());

            default -> "";
        };
    }

    private LocalDate getDateCell(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (DateUtil.isCellDateFormatted(cell)) {

            return cell.getLocalDateTimeCellValue()
                    .toLocalDate();
        }

        throw new RuntimeException("Ngày sinh không hợp lệ");
    }

    private boolean isEmptyRow(Row row) {

        for (Cell cell : row) {

            if (!getStringCell(cell).isBlank()) {
                return false;
            }
        }

        return true;
    }

    // Chọn leader của team
    public TeamLeaderResponse assignLeader(
            UUID teamId,
            AssignTeamLeaderRequest request) {

        RescueTeam team = rescueTeamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESCUE_TEAM_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_EXISTED));

        if (user.getRole() != Role.RESCUER) {
            throw new AppException(
                    ErrorCode.USER_IS_NOT_RESCUER);
        }

        if (user.getTeam() == null
                || !user.getTeam().getId().equals(teamId)) {
            throw new AppException(
                    ErrorCode.RESCUER_NOT_IN_TEAM);
        }

        team.setLeader(user);

        team = rescueTeamRepository.save(team);

        return TeamLeaderResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .leaderId(user.getId())
                .leaderName(user.getHoten())
                .build();
    }

    // Danh sách leader theo khu vực
    @Transactional(readOnly = true) // readOnly = true vì chỉ đọc data
    public List<TeamLeaderItemResponse> getLeadersByArea(
            UUID areaId) {

        return rescueTeamRepository.findByAreaId(areaId)
                .stream()
                .filter(team -> team.getLeader() != null)
                .map(team -> TeamLeaderItemResponse.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .leaderId(team.getLeader().getId())
                        .leaderName(team.getLeader().getHoten())
                        .phone(team.getLeader().getSodt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RescueGroupResponse> getListGroupOfTeam(UUID teamId, Pageable pageable) {
        Page<RescueGroupResponse> page = rescueGroupRepository.findGroupByTeamId(teamId, pageable);
        if (page.isEmpty()) {
            throw new AppException(ErrorCode.LIST_GROUP_NOT_FOUND);
        }
        return page;
    }

    public RescueTeamResponse getDetailTeam(UUID teamId) {
        RescueTeamResponse team = rescueTeamRepository.findDetail(teamId);
        if (team == null) {
            throw new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND);
        }
        return team;
    }

    // List team by area level 1
    public Page<RescueTeamResponse> getListTeamByArea(UUID areaId, Pageable pageable) {
        Page<RescueTeamResponse> page = rescueTeamRepository.findByAreaId(areaId, pageable);
        if (page.isEmpty()) {
            throw new AppException(ErrorCode.LIST_TEAM_NOT_FOUND);
        }
        return page;
    }

    // Cập nhật thông tin đội
    @Transactional
    public RescueTeamResponse update(
            UUID teamId,
            UpdateRescueTeamRequest request) {

        RescueTeam team = rescueTeamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

        User currentUser = authenticationService.getCurrentUser();

        // Chỉ Admin hoặc đội trưởng của chính đội đó được cập nhật
        if (currentUser.getRole() != Role.ADMIN) {

            if (team.getLeader() == null
                    || !team.getLeader().getId().equals(currentUser.getId())) {

                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        // Kiểm tra trùng tên
        if (!team.getName().equals(request.getName())
                && rescueTeamRepository.existsByName(request.getName())) {

            throw new AppException(ErrorCode.RESCUE_TEAM_EXISTED);
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setEmergencyPhone(request.getEmergencyPhone());

        team = rescueTeamRepository.save(team);

        return RescueTeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .areaId(team.getArea().getId())
                .areaName(team.getArea().getTenkhuvuc())
                .leaderId(team.getLeader() != null
                        ? team.getLeader().getId()
                        : null)
                .leaderName(team.getLeader() != null
                        ? team.getLeader().getHoten()
                        : null)
                .emergencyPhone(team.getEmergencyPhone())
                .lat(team.getLat())
                .lon(team.getLon())
                .build();
    }

    // Xóa member trong team, chỉ team leader mới được xóa, ko xóa group leader
    @Transactional
    public void deleteMember(UUID teamId, UUID userId) {

        User currentUser = authenticationService.getCurrentUser();

        RescueTeam team = rescueTeamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESCUE_TEAM_NOT_FOUND));

        // Chỉ Team Leader được xóa
        if (team.getLeader() == null
                || !team.getLeader().getId().equals(currentUser.getId())) {

            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_EXISTED));

        // Phải thuộc đúng team
        if (!user.getTeam().getId().equals(teamId)) {
            throw new AppException(ErrorCode.RESCUER_NOT_IN_TEAM);
        }

        // Chỉ được xóa RESCUER
        if (user.getRole() != Role.RESCUER) {
            throw new AppException(ErrorCode.USER_IS_NOT_RESCUER);
        }

        // Không được xóa nếu đang là Group Leader
        if (rescueGroupRepository.existsByLeaderId(userId)) {
            throw new AppException(ErrorCode.GROUP_LEADER_CANNOT_DELETE);
        }

        // Loại khỏi team và vô hiệu hóa tài khoản
        user.setTrangthai(Status.INACTIVE);
        user.setTeam(null);

        userRepository.save(user);
    }
}
