package com.finanzas.app.domain.model;

import com.finanzas.app.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")                         // ← BD: "transaction" sin s
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;                    // ← BD: tipo USER-DEFINED (transaction_type enum)

    @Column(nullable = false)
    private String category;                         // ← campo nuevo agregado a la BD

    @Column(name = "transactiondate")                // ← BD: "transactiondate" sin guión bajo
    private LocalDateTime transactionDate;           // ← BD: timestamp, cambiado de LocalDate a LocalDateTime

    private String notes;                            // ← campo nuevo agregado a la BD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")                     // ← campo nuevo agregado a la BD
    private LocalDateTime createdAt;
}