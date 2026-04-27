package com.finanzas.app.service;

import com.finanzas.app.presentation.dto.request.BudgetRequest;
import com.finanzas.app.presentation.dto.response.BudgetResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse create(Long userId, BudgetRequest request);
    BudgetResponse update(Long userId, Long budgetId, BudgetRequest request);
    List<BudgetResponse> findByMonth(Long userId, int month, int year);
}