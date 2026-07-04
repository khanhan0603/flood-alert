package com.example.flood_alert.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.SosRequestRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackingCodeGenerator {

    private static final int TRACKING_CODE_LENGTH = 6;

    private static final int MAX_RETRY = 5;

    private static final String UNIQUE_TRACKING_CONSTRAINT = "uq_sos_tracking_code";

    SosRequestRepository sosRequestRepository;

    /**
     * Sinh trackingCode và lưu SOS.
     * Nếu trùng trackingCode sẽ tự sinh lại tối đa MAX_RETRY lần.
     */
    public SosRequest save(SosRequest sos) {

        for (int i = 0; i < MAX_RETRY; i++) {

            sos.setTrackingCode(generate());

            try {

                return sosRequestRepository.saveAndFlush(sos);

            } catch (DataIntegrityViolationException ex) {

                if (!isTrackingCodeDuplicate(ex)) {
                    throw ex;
                }
            }
        }

        throw new AppException(ErrorCode.GENERATE_TRACKING_CODE_FAILED);
    }

    /**
     * Sinh mã tra cứu gồm 6 ký tự chữ và số.
     */
    private String generate() {

        return RandomStringUtils
                .randomAlphanumeric(TRACKING_CODE_LENGTH)
                .toUpperCase();
    }

    /**
     * Chỉ retry khi UNIQUE tracking_code bị trùng.
     */
    private boolean isTrackingCodeDuplicate(
            DataIntegrityViolationException ex) {

        Throwable cause = ex.getMostSpecificCause();

        return cause != null
                && cause.getMessage() != null
                && cause.getMessage().contains(UNIQUE_TRACKING_CONSTRAINT);
    }
}