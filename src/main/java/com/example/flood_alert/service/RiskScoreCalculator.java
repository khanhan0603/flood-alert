package com.example.flood_alert.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.enums.RiskLevel;

@Component
public class RiskScoreCalculator {

    private static final double DANGER_RATIO_THRESHOLD = 0.5;

    public RiskLevel calculate(List<IoTAreaAggregates> aggregates, FloodPrediction prediction) {

        RiskLevel iotRisk = calculateIotRisk(aggregates);

        return combineRisk(iotRisk, prediction);
    }

    /**
     * Kết hợp dữ liệu IoT và kết quả dự báo AI.
     *
     * IoT là nguồn dữ liệu chính.
     * AI chỉ đóng vai trò cảnh báo sớm.
     */
    private RiskLevel combineRisk(RiskLevel iotRisk, FloodPrediction prediction) {

        if (prediction == null) {
            return iotRisk;
        }

        RiskLevel aiRisk = prediction.getLead1();

        if (iotRisk == RiskLevel.HIGH) {
            return RiskLevel.HIGH;
        }

        if (iotRisk == RiskLevel.MEDIUM) {
            return RiskLevel.MEDIUM;
        }

        // IoT LOW + AI HIGH => MEDIUM
        if (iotRisk == RiskLevel.LOW && aiRisk == RiskLevel.HIGH) {

            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    /**
     * Đánh giá mức độ rủi ro của khu vực dựa trên
     * dữ liệu IoT Aggregate trong 2 phút gần nhất.
     *
     * HIGH:
     * - Ít nhất 50% aggregate ở trạng thái nguy hiểm
     * - Mực nước 5 lần đo gần nhất không giảm
     *
     * MEDIUM:
     * - Ít nhất 30% aggregate ở trạng thái nguy hiểm
     *
     * LOW:
     * - Các trường hợp còn lại
     */
    private RiskLevel calculateIotRisk(List<IoTAreaAggregates> aggregates) {

        if (aggregates == null || aggregates.isEmpty()) {
            return RiskLevel.LOW;
        }

        long dangerCount = aggregates.stream()
                .filter(a -> a.getDangerRatio() != null
                        && a.getDangerRatio() >= DANGER_RATIO_THRESHOLD)
                .count();

        double dangerPercent = (double) dangerCount / aggregates.size();

        boolean recentTrendNotDecreasing = isRecentTrendNotDecreasing(aggregates);

        if (dangerPercent >= 0.5
                && recentTrendNotDecreasing) {

            return RiskLevel.HIGH;
        }

        if (dangerPercent >= 0.3) {

            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    // Tính điểm rủi ro trung bình của khu vực
    public double calculateScore(
            List<IoTAreaAggregates> aggregates) {

        return aggregates.stream()
                .mapToDouble(a -> a.getDangerRatio() == null
                        ? 0.0
                        : a.getDangerRatio())
                .average()
                .orElse(0.0);
    }

    /**
     * Kiểm tra xu hướng mực nước
     * của 5 aggregate gần nhất (~10 phút).
     *
     * Trả về true nếu mực nước giữ nguyên
     * hoặc tăng liên tục.
     *
     * Trả về false nếu xuất hiện xu hướng giảm.
     */
    private boolean isRecentTrendNotDecreasing(List<IoTAreaAggregates> aggregates) {
        if (aggregates.size() < 2)
            return false;

        //lấy tối đa 5, không hardcode subList(0, 5)
        int trendWindow = Math.min(5, aggregates.size());
        List<IoTAreaAggregates> last5 = new ArrayList<>(
                aggregates.subList(0, trendWindow));

        // Repository ORDER BY recordedAt DESC → đảo về tăng dần
        Collections.reverse(last5);

        for (int i = 1; i < last5.size(); i++) {
            Double previous = last5.get(i - 1).getCurrentWater();
            Double current = last5.get(i).getCurrentWater();

            if (previous == null || current == null)
                return false;
            if (current < previous)
                return false;
        }

        return true;
    }

    // Đếm số lượng dữ liệu tổng hợp là nguy hiểm trong 30 phút gần nhất
    public long countDangerAggregates(
            List<IoTAreaAggregates> aggregates) {

        return aggregates.stream()
                .filter(a -> a.getDangerRatio() != null
                        && a.getDangerRatio() >= DANGER_RATIO_THRESHOLD)
                .count();
    }
}