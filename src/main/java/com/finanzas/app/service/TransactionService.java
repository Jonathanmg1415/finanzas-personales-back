package com.finanzas.app.service;

import com.finanzas.app.presentation.dto.request.TransactionRequest;
import com.finanzas.app.presentation.dto.response.TransactionResponse;
import com.finanzas.app.presentation.dto.response.BalanceResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse create(Long userId, TransactionRequest request);
    List<TransactionResponse> findAllByUser(Long userId);
    TransactionResponse findById(Long id, Long userId);
    TransactionResponse update(Long id, Long userId, TransactionRequest request);
    void delete(Long id, Long userId);
    BalanceResponse getMonthlyBalance(Long userId, int month, int year);
}
