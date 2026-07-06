package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;

import java.util.List;
// Class này là repository tập hợp các thông báo về nhiệm vụ: group thất bại, sos quá hạn, yêu cầu hỗ trợ... 
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByChannelAndStatus(
            Channel channel,
            StatusAlert status);
}