package com.finanzas.app.service.impl;

import com.finanzas.app.domain.enums.TransactionType;
import com.finanzas.app.domain.model.Transaction;
import com.finanzas.app.domain.repository.TransactionRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.TransactionRequest;
import com.finanzas.app.presentation.dto.response.BalanceResponse;
import com.finanzas.app.presentation.dto.response.TransactionResponse;
import com.finanzas.app.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var transaction = Transaction.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .user(user)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllByUser(Long userId) {
        return transactionRepository
                .findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id, Long userId) {
        return toResponse(getTransactionOfUser(id, userId));
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, Long userId, TransactionRequest request) {
        var transaction = getTransactionOfUser(id, userId);

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        var transaction = getTransactionOfUser(id, userId);
        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getMonthlyBalance(Long userId, int month, int year) {
        BigDecimal totalIncome = transactionRepository
                .sumByUserIdAndTypeAndPeriod(userId, TransactionType.INCOME, month, year);
        BigDecimal totalExpense = transactionRepository
                .sumByUserIdAndTypeAndPeriod(userId, TransactionType.EXPENSE, month, year);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        String status;
        if (balance.compareTo(BigDecimal.ZERO) > 0)      status = "SUPERAVIT";
        else if (balance.compareTo(BigDecimal.ZERO) < 0) status = "DEFICIT";
        else                                              status = "EQUILIBRIO";

        return BalanceResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .status(status)
                .build();
    }

    private Transaction getTransactionOfUser(Long id, Long userId) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para acceder a esta transacción");
        }
        return transaction;
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .description(t.getDescription())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
