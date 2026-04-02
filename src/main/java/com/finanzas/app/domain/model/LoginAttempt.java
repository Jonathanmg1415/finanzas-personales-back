package com.finanzas.app.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "attempts", nullable = false)
    private int attempts;                            // ← BD: "attempts" (antes era failedAttempts)

    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt;               // ← BD: "last_attempt" nullable (antes era lastAttemptAt)

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;               // ← BD: "locked_until" nullable (antes era blockedUntil)
}