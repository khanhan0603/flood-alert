package com.example.flood_alert.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;



public interface UserRepository extends JpaRepository<User,UUID> {
    boolean existsByRole(Role role);
    boolean existsByEmail(String email);
    boolean existsBySodt(String sodt);
    Optional<User> findByEmailOrSodt(String email, String sodt);
    List<User> findByAreaAndTrangthai(Area area,Status status);
}
