package com.finanzas.app.presentation.dto.request;

import com.finanzas.app.domain.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255)
    private String description;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.0", inclusive = false,
                message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @NotNull(message = "El tipo es requerido")
    private TransactionType type;

    @NotNull(message = "Seleccionar una categoría es obligatorio")
    private Long categoryId;

    @NotNull(message = "La fecha es requerida")
    private LocalDateTime transactionDate;           // ← cambiado de LocalDate a LocalDateTime (BD: timestamp)

    private String notes;
}