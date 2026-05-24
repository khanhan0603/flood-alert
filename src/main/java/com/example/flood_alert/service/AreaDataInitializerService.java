package com.example.flood_alert.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.repository.AreaRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaDataInitializerService {

    AreaRepository areaRepository;

    GeometryFactory geometryFactory =
        new GeometryFactory();

    public void init() {

        try {

            long wardCount = areaRepository.countByLevel(2);
        
            if (wardCount >= 3321) {
                log.info("AREA ALREADY EXISTS");
                return;
            }

            log.info(
                "START IMPORT AREA"
            );

            importProvince();

            importWard();

            log.info(
                "DONE IMPORT AREA"
            );

        } catch (Exception e) {

            log.error(
                "ERROR IMPORT",
                e
            );
        }
    }

    // ==========================================
    // IMPORT PROVINCE
    // ==========================================
    public void importProvince()
        throws Exception {

        log.info(
            "START IMPORT PROVINCE"
        );

        InputStream inputStream =
            new ClassPathResource(
                "data/province_boundary_wkt.csv"
            ).getInputStream();

        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(
                    inputStream,
                    StandardCharsets.UTF_8
                )
            );

        WKTReader wktReader =
            new WKTReader();

        // skip header
        reader.readLine();

        String line;

        int count = 0;

        while (
            (line = reader.readLine())
            != null
        ) {
            
            List<String> parts =
                parseCsvColumns(
                    line,
                    4
                );

            if (parts.size() < 4) {
                continue;
            }

            String provinceName =
                cleanCsvValue(
                    parts.get(0)
                );

            double lat =
                Double.parseDouble(
                    parts.get(1)
                );

            double lon =
                Double.parseDouble(
                    parts.get(2)
                );

            String wkt =
                cleanCsvValue(
                    parts.get(3)
                );

            log.info(
                "IMPORT PROVINCE: {}",
                provinceName
            );

            // check exist
            boolean exists =
                areaRepository
                    .existsByTenkhuvuc(
                        provinceName
                    );

            if (exists) {
                continue;
            }

            MultiPolygon polygon =
                toMultiPolygon(
                    wktReader.read(wkt)
                );

            Area province =
                Area.builder()
                    .tenkhuvuc(
                        provinceName
                    )
                    .mota(
                        "Tỉnh/Thành phố"
                    )
                    .level(1)
                    .lat(
                        BigDecimal.valueOf(
                            lat
                        )
                    )
                    .lon(
                        BigDecimal.valueOf(
                            lon
                        )
                    )
                    .polygon(
                        polygon
                    )
                    .build();

            areaRepository.save(
                province
            );

            count++;

            if (count % 20 == 0) {
                log.info(
                    "IMPORTED PROVINCE: {}",
                    count
                );
            }
        }

        reader.close();

        log.info(
            "DONE IMPORT PROVINCE"
        );
    }

    // ==========================================
    // IMPORT WARD
    // ==========================================
    public void importWard()
        throws Exception {

        log.info(
            "START IMPORT WARD"
        );

        InputStream inputStream =
            new ClassPathResource(
                "data/ward_boundary_wkt_full_34.csv"
            ).getInputStream();

        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(
                    inputStream,
                    StandardCharsets.UTF_8
                )
            );

        WKTReader wktReader =
            new WKTReader();

        // skip header
        reader.readLine();

        String line;

        int count = 0;

        while (
            (line = reader.readLine())
            != null
        ) {

            List<String> parts =
                parseCsvColumns(
                    line,
                    5
                );

            if (parts.size() < 5) {
                continue;
            }

            String wardName =
                cleanCsvValue(
                    parts.get(0)
                );

            String provinceName =
                cleanCsvValue(
                    parts.get(1)
                );

            // FIXED LAT/LON
            double lat =
                Double.parseDouble(
                    parts.get(2)
                );

            double lon =
                Double.parseDouble(
                    parts.get(3)
                );

            String wkt =
                cleanCsvValue(
                    parts.get(4)
                );

            log.info(
                "IMPORT WARD: {} - {}",
                wardName,
                provinceName
            );

            MultiPolygon polygon =
                toMultiPolygon(
                    wktReader.read(wkt)
                );

            Area province =
                areaRepository
                    .findByTenkhuvuc(
                        provinceName
                    )
                    .orElseThrow();

            boolean exists =
                areaRepository
                    .existsByTenkhuvucAndParent(
                        wardName,
                        province
                    );

            if (exists) {
                continue;
            }

            Area ward =
                Area.builder()
                    .tenkhuvuc(
                        wardName
                    )
                    .mota(
                        "Xã/Phường"
                    )
                    .level(2)
                    .lat(
                        BigDecimal.valueOf(
                            lat
                        )
                    )
                    .lon(
                        BigDecimal.valueOf(
                            lon
                        )
                    )
                    .parent(
                        province
                    )
                    .polygon(
                        polygon
                    )
                    .build();

            areaRepository.save(
                ward
            );

            count++;

            if (count % 100 == 0) {
                log.info(
                    "IMPORTED WARD: {}",
                    count
                );
            }
        }

        reader.close();

        log.info(
            "DONE IMPORT WARD"
        );
    }

    private List<String> parseCsvColumns(
        String line,
        int expectedColumns
    ) {

        List<String> columns =
            new ArrayList<>(
                expectedColumns
            );

        int start = 0;

        for (
            int column = 1;
            column < expectedColumns;
            column++
        ) {

            int commaIndex =
                line.indexOf(
                    ',',
                    start
                );

            if (commaIndex < 0) {
                break;
            }

            columns.add(
                line.substring(
                    start,
                    commaIndex
                )
            );

            start =
                commaIndex + 1;
        }

        if (start <= line.length()) {

            columns.add(
                line.substring(start)
            );
        }

        return columns;
    }

    private String cleanCsvValue(
        String value
    ) {

        String cleaned =
            value.trim();

        if (
            cleaned.length() >= 2
            && cleaned.startsWith("\"")
            && cleaned.endsWith("\"")
        ) {

            cleaned =
                cleaned.substring(
                    1,
                    cleaned.length() - 1
                );
        }

        return cleaned
            .replace(
                "\"\"",
                "\""
            )
            .trim();
    }

    private MultiPolygon toMultiPolygon(
        Geometry geometry
    ) {

        geometry.setSRID(4326);

        if (
            geometry instanceof Polygon polygon
        ) {

            MultiPolygon multiPolygon =
                geometryFactory
                    .createMultiPolygon(
                        new Polygon[]{
                            polygon
                        }
                    );

            multiPolygon.setSRID(4326);

            return multiPolygon;
        }

        MultiPolygon multiPolygon =
            (MultiPolygon) geometry;

        multiPolygon.setSRID(4326);

        return multiPolygon;
    }
}
