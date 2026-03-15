package com.finanzas.app.presentation.dto.request;

import com.finanzas.app.domain.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255)
    private String description;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "El tipo es requerido")
    private TransactionType type;

    @NotBlank(message = "La categoría es requerida")
    private String category;

    @NotNull(message = "La fecha es requerida")
    private LocalDate transactionDate;

    private String notes;
}
