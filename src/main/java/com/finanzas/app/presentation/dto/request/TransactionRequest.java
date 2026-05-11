package com.finanzas.app.presentation.dto.request;

import com.finanzas.app.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Datos para registrar o actualizar una transacción financiera")
public class TransactionRequest {

    @Schema(description = "Descripción de la transacción", example = "Supermercado Éxito")
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255)
    private String description;

    @Schema(description = "Monto de la transacción — debe ser mayor a cero", example = "150000")
    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @Schema(description = "Tipo de transacción", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
    @NotNull(message = "El tipo es requerido")
    private TransactionType type;

    @Schema(description = "ID de la categoría asociada (obtenido de GET /api/v1/categories)", example = "1")
    @NotNull(message = "Seleccionar una categoría es obligatorio")
    private Long categoryId;

    @Schema(description = "Fecha y hora de la transacción", example = "2026-04-01T10:00:00")
    @NotNull(message = "La fecha es requerida")
    private LocalDateTime transactionDate;

    @Schema(description = "Nota descriptiva opcional", example = "Compras de la semana", nullable = true)
    private String notes;
}