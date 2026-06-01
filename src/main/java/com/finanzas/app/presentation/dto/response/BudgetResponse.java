package com.finanzas.app.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetResponse {
    private Long id;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spent;
    private int month;
    private int year;
    private BigDecimal alertThreshold;

    // HU-24: Porcentaje de uso del presupuesto (0.0 a 100.0)
    private BigDecimal usagePercentage;

    // HU-24: true si spent >= alertThreshold * limitAmount
    private Boolean alertTriggered;

    // HU-24: Mensaje descriptivo cuando se activa la alerta
    private String alertMessage;

    private String message;
}