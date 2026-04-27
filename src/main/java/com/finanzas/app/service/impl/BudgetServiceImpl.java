package com.finanzas.app.service.impl;

import com.finanzas.app.domain.model.Budget;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.BudgetRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.BudgetRequest;
import com.finanzas.app.presentation.dto.response.BudgetResponse;
import com.finanzas.app.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BudgetResponse create(Long userId, BudgetRequest request) {
        // Solo un presupuesto activo por categoría al mes
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, request.getCategory(), request.getMonth(), request.getYear())
                .ifPresent(b -> { throw new IllegalArgumentException(
                        "Ya existe un presupuesto activo para esta categoría en el mes actual"); });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Budget budget = Budget.builder()
                .category(request.getCategory())
                .limitAmount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .spent(BigDecimal.ZERO)
                .user(user)
                .build();

        return toResponse(budgetRepository.save(budget), "Presupuesto creado exitosamente");
    }

    @Override
    @Transactional
    public BudgetResponse update(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para modificar este presupuesto");
        }

        budget.setLimitAmount(request.getLimitAmount());
        budget.setCategory(request.getCategory());

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

    private BudgetResponse toResponse(Budget b, String message) {
        return BudgetResponse.builder()
                .id(b.getId())
                .category(b.getCategory())
                .limitAmount(b.getLimitAmount())
                .spent(b.getSpent())
                .month(b.getMonth())
                .year(b.getYear())
                .message(message)
                .build();
    }
}