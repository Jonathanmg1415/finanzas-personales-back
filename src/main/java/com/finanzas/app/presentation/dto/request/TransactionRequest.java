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

    // HU-20: el monto debe ser mayor a cero
    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.0", inclusive = false,
                message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @NotNull(message = "El tipo es requerido")
    private TransactionType type;

    // HU-20: la categoría es obligatoria
    @NotBlank(message = "Seleccionar una categoría es obligatorio")
    private String category;

    @NotNull(message = "La fecha es requerida")
    private LocalDate transactionDate;

    private String notes;
}