package com.finanzas.app.presentation.dto.response;

import com.finanzas.app.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class TransactionResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate transactionDate;
    private String notes;
    private LocalDateTime createdAt;
}
