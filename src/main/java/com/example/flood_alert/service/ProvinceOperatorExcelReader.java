package com.example.flood_alert.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.ProvinceOperatorExcelRow;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProvinceOperatorExcelReader {
    DataFormatter formatter = new DataFormatter();

    public List<ProvinceOperatorExcelRow> read(MultipartFile file)
            throws IOException {
        List<ProvinceOperatorExcelRow> result = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // Dòng 0->4 là tiêu đề
            // Dữ liệu bắt đầu từ dòng 5 (Excel row 6)
            for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String hoten = getCellValue(row, 1);
                if (hoten == null || hoten.isBlank()) {
                    continue;
                }
                String gioitinh = getCellValue(row, 2);
                Boolean gender = null;

                if ("Nam".equalsIgnoreCase(gioitinh)) {
                    gender = true;
                } else if ("Nữ".equalsIgnoreCase(gioitinh)) {
                    gender = false;
                }

                String ngaySinhText = getCellValue(row, 3);

                LocalDate ngaysinh = parseDate(row, 3, ngaySinhText);

                String sodt = normalizePhone(getCellValue(row, 4));
                String email = getCellValue(row, 5);
                String diachi=getCellValue(row, 6);
                String provinceName = getCellValue(row, 7);

                result.add(
                        ProvinceOperatorExcelRow.builder()
                                .rowNumber(i+1)
                                .hoten(hoten)
                                .gioitinh(gender)
                                .ngaysinh(ngaysinh)
                                .sodt(sodt)
                                .email(email)
                                .diachi(diachi)
                                .provinceName(provinceName)
                                .build());
            }
        }
        return result;
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

    private LocalDate parseDate(
            Row row,
            int cellIndex,
            String value) {

        Cell cell = row.getCell(cellIndex);

        if (cell == null) {
            return null;
        }

        try {

            // Excel lưu kiểu Date
            if (DateUtil.isCellDateFormatted(cell)) {

                return cell.getDateCellValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            // Excel lưu kiểu text: 06/03/2004
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            return LocalDate.parse(
                    value.trim(),
                    formatter);

        } catch (Exception e) {

            log.warn(
                    "Không đọc được ngày sinh: {}",
                    value);

            return null;
        }
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return null;
        }
        return formatter.formatCellValue(row.getCell(cellIndex))
                .trim();
    }
}
