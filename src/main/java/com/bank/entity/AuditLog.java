package com.bank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String actionType; // LOGIN, TRANSFER, CARD_CREATE, etc.

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 50)
    private String resourceId; // Card ID, Transaction ID, etc.

    @Column(length = 1000)
    private String requestDetails; // JSON with request details
}
