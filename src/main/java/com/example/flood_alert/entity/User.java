package com.example.flood_alert.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_sodt", columnNames = "sodt")
})
public class User extends BaseEntity {
    String hoten;
    boolean gioitinh;
    LocalDate ngaysinh;
    @Column(nullable = false)
    String sodt;
    String diachi;
    @Column(nullable = false)
    String email;
    @Column(nullable = false)
    String password;

    @Builder.Default // giữ giá trị mặc định cho role
    @Enumerated(EnumType.STRING)
    Role role = Role.CITIZEN;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    Status trangthai = Status.ACTIVE;

    @Column(columnDefinition = "TEXT")
    String ghichu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    Area area;

    LocalDateTime created_at;
    LocalDateTime updated_at;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    List<SosRequest> sosList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    RescueTeam team;
}
