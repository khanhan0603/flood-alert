package com.example.flood_alert.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.FloodAlert;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.FloodAlertRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal=true)
public class EmailProcessor {
    FloodAlertRepository floodAlertRepository;
    EmailService emailService;

    @Transactional
    public void processPendingEmails(){
        List<FloodAlert> alerts=floodAlertRepository.findByChannelAndStatus(Channel.EMAIL,StatusAlert.PENDING);
        
        for(FloodAlert alert:alerts){
            try {
                emailService.sendEmail(alert.getUser().getEmail(), alert.getTitle(), alert.getMessage());

                alert.setStatus(StatusAlert.SENT);
                alert.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error(
                    "Gửi email thất bại={}",
                    alert.getId(),
                    e
                );
                alert.setStatus(StatusAlert.FAILED);
            }
        }
        floodAlertRepository.saveAll(alerts);
    }
}
