package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Entity lưu blacklist token khi logout
@Entity
@Table(name = "invalidated_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    String jwtId;

    @Column(nullable = false)
    LocalDateTime expiryTime;

}
