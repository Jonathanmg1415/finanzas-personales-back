package com.finanzas.app.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull(message = "La categoría es requerida")
    private Long categoryId;

    @NotNull(message = "El monto límite es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
    private BigDecimal limitAmount;

    @NotNull(message = "El mes es requerido")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer month;

    @NotNull(message = "El año es requerido")
    @Min(value = 2000, message = "El año no es válido")
    private Integer year;

    // HU-24: Umbral de alerta opcional — entre 0.01 y 1.0 — por defecto 0.80 (80%)
    @DecimalMin(value = "0.01", message = "El umbral debe ser mayor a 0")
    @DecimalMax(value = "1.0", message = "El umbral no puede superar 1.0 (100%)")
    private BigDecimal alertThreshold;
}