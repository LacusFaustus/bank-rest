package com.bank.repository;

import com.bank.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByActionType(String actionType, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:username IS NULL OR a.username = :username) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:success IS NULL OR a.success = :success) AND " +
            "a.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findWithFilters(@Param("username") String username,
                                   @Param("actionType") String actionType,
                                   @Param("success") Boolean success,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username AND a.timestamp > :since")
    long countRecentActionsByUser(@Param("username") String username,
                                  @Param("since") LocalDateTime since);
}
