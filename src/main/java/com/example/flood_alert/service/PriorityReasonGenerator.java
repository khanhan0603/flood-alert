package com.example.flood_alert.service;

import org.springframework.stereotype.Component;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;

@Component
public class PriorityReasonGenerator {

    public String generate(
            Priority priority,
            Integer victimCount,
            Boolean injured,
            Boolean trapped,
            Boolean vulnerable,
            EnvironmentRisk environmentRisk) {

        StringBuilder condition = new StringBuilder();

        if (Boolean.TRUE.equals(trapped)) {
            condition.append("có người mắc kẹt, ");
        }

        if (Boolean.TRUE.equals(injured)) {
            condition.append("có người bị thương, ");
        }

        if (Boolean.TRUE.equals(vulnerable)) {
            condition.append("có đối tượng dễ bị tổn thương, ");
        }

        if (condition.length() == 0) {
            condition.append("không có tình trạng đặc biệt, ");
        }

        String environmentText = switch (environmentRisk) {

            case HIGH -> "khu vực nguy hiểm cao";

            case MEDIUM -> "khu vực nguy hiểm trung bình";

            default -> "khu vực nguy hiểm thấp";
        };

        return priority.name()
                + ": "
                + condition.substring(0, condition.length() - 2)
                + ", "
                + environmentText
                + ".";
    }
}
