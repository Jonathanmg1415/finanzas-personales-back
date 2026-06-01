package com.finanzas.app.service.impl;

import com.finanzas.app.domain.model.Budget;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.BudgetRepository;
import com.finanzas.app.domain.repository.CategoryRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.BudgetRequest;
import com.finanzas.app.presentation.dto.response.BudgetResponse;
import com.finanzas.app.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.80");

    private final BudgetRepository   budgetRepository;
    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BudgetResponse create(Long userId, BudgetRequest request) {
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.getCategoryId(), request.getMonth(), request.getYear())
                .ifPresent(b -> { throw new IllegalArgumentException(
                        "Ya existe un presupuesto activo para esta categoría en el mes actual"); });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        BigDecimal threshold = request.getAlertThreshold() != null
                ? request.getAlertThreshold()
                : DEFAULT_THRESHOLD;

        Budget budget = Budget.builder()
                .category(category)
                .limitAmount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .spent(BigDecimal.ZERO)
                .alertThreshold(threshold)
                .user(user)
                .build();

        return toResponse(budgetRepository.save(budget), "Presupuesto creado exitosamente");
    }

    @Override
    @Transactional
    public BudgetResponse update(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));

        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para modificar este presupuesto");
        }

        budget.setLimitAmount(request.getLimitAmount());
        budget.setCategory(category);

        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }

        return toResponse(budgetRepository.save(budget), "Presupuesto actualizado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> findByMonth(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(b -> toResponse(b, null))
                .toList();
    }

    // ─── HU-24: Motor de evaluación de umbrales ───────────────────────────────

    private BudgetResponse toResponse(Budget b, String message) {
        BigDecimal usagePercentage = BigDecimal.ZERO;
        boolean alertTriggered = false;
        String alertMessage = null;

        if (b.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = b.getSpent()
                    .divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal thresholdAmount = b.getLimitAmount()
                    .multiply(b.getAlertThreshold())
                    .setScale(2, RoundingMode.HALF_UP);

            if (b.getSpent().compareTo(thresholdAmount) >= 0) {
                alertTriggered = true;

                if (b.getSpent().compareTo(b.getLimitAmount()) >= 0) {
                    alertMessage = String.format(
                            "Has superado el presupuesto de %s. Gastado: %.0f / Límite: %.0f",
                            b.getCategory().getName(),
                            b.getSpent().doubleValue(),
                            b.getLimitAmount().doubleValue());
                } else {
                    alertMessage = String.format(
                            "Has alcanzado el %.0f%% del presupuesto de %s. Gastado: %.0f / Límite: %.0f",
                            usagePercentage.doubleValue(),
                            b.getCategory().getName(),
                            b.getSpent().doubleValue(),
                            b.getLimitAmount().doubleValue());
                }
            }
        }

        return BudgetResponse.builder()
                .id(b.getId())
                .categoryName(b.getCategory().getName())
                .limitAmount(b.getLimitAmount())
                .spent(b.getSpent())
                .month(b.getMonth())
                .year(b.getYear())
                .alertThreshold(b.getAlertThreshold())
                .usagePercentage(usagePercentage)
                .alertTriggered(alertTriggered)
                .alertMessage(alertMessage)
                .message(message)
                .build();
    }
}