package com.finanzas.app.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class BalanceResponse {
    private int month;
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private String status; // SUPERAVIT / DEFICIT / EQUILIBRIO
}
