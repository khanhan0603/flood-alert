package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsBySodt(String sodt);

    Optional<User> findByEmailOrSodt(String email, String sodt);

    Optional<User> findByEmail(String email);

    Optional<User> findBySodt(String sodt);

    List<User> findByAreaAndTrangthai(Area area, Status status);

    // Load toàn bộ email, sdt trong table
    @Query("""
                select u.email
                from User u
            """)
    Set<String> findAllEmails();

    @Query("""
                select u.sodt
                from User u
            """)
    Set<String> findAllPhones();

    //List các rescuer có team nhưng chưa thuộc group nào
    @Query("""
                SELECT u
                FROM User u
                WHERE u.role = com.example.flood_alert.enums.Role.RESCUER
                  AND u.team.id = :teamId
                  AND NOT EXISTS (
                        SELECT rgm
                        FROM RescueGroupMember rgm
                        WHERE rgm.user.id = u.id
                  )
            """)
    List<User> findAvailableMembers(UUID teamId);
}
