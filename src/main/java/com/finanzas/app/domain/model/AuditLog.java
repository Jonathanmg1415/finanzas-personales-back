package com.finanzas.app.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Acción realizada: CREATE, UPDATE, DELETE, LOGIN, LOGIN_FAILED, LOGOUT
    @Column(nullable = false, length = 50)
    private String action;

    // Entidad afectada: TRANSACTION, BUDGET, CATEGORY, AUTH
    @Column(nullable = false, length = 50)
    private String entity;

    // ID del registro afectado (null para eventos de auth)
    @Column(name = "entity_id")
    private Long entityId;

    // Descripción legible del evento
    @Column(length = 500)
    private String description;

    // IP desde donde se realizó la acción
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}