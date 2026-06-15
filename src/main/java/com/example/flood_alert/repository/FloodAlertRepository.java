package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.FloodAlert;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;

public interface FloodAlertRepository extends JpaRepository<FloodAlert, UUID> {
    // Lấy alert gần nhất của khu vực
    Optional<FloodAlert> findTopByAreaOrderByCreatedAtDesc(Area area);

    // Lấy alert gần nhất theo channel
    Optional<FloodAlert> findTopByAreaAndChannelOrderByCreatedAtDesc(Area area, Channel channel);

    // Lấy lịch sử cảnh báo
    Page<FloodAlert> findByUser(User user, Pageable pageable);

    // Lấy cảnh báo theo khu vực
    Page<FloodAlert> findByArea(Area area, Pageable pageable);

    List<FloodAlert> findByChannelAndStatus(Channel channel, StatusAlert status);

    // Lấy danh sách lịch sử alert theo userId order by mới nhất
    Page<FloodAlert> findByUserOrderByCreatedAtDesc(
            User user,
            Pageable pageable);
}
