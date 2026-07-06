package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.RescueGroupType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rescue_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    RescueTeam team;

    @Column(nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    User leader;

    //Group hotline đã được tạo mặc định khi tạo đội nên team leader tạo thêm nhóm thì sẽ
    //mặc định là nhóm type vận hành
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RescueGroupType type = RescueGroupType.OPERATIONAL;

    @Enumerated(EnumType.STRING)
    RescueGroupStatus status;

    Double currentLat;

    Double currentLon;

    boolean hasBoat;

    boolean hasMedical;

    // Nhóm tìm kiếm cứu nạn
    boolean hasSearchRescue;

    // Nhóm hậu cần: nhu yếu phẩm, đồ ăn, thức uống
    boolean hasLogistics;

    @Column(columnDefinition = "TEXT")
    String notes;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
