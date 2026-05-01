package com.finanzas.app.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budget")                              // ← BD: "budget" sin s
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)     // ← nueva clave foranea añadida a la db
    private Category category;

    @Column(name = "limitamount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;                  // ← BD: "limitamount" sin guión bajo

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spent = BigDecimal.ZERO;      // ← campo nuevo agregado a la BD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}