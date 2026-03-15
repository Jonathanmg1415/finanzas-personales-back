package com.finanzas.app.service;

import com.finanzas.app.domain.enums.TransactionType;
import com.finanzas.app.domain.model.Transaction;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.TransactionRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.TransactionRequest;
import com.finanzas.app.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TransactionServiceImpl transactionService;

    private User testUser;
    private TransactionRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@finanzas.com")
                .fullName("Usuario Test")
                .build();

        testRequest = new TransactionRequest();
        testRequest.setDescription("Salario mensual");
        testRequest.setAmount(new BigDecimal("3500000"));
        testRequest.setType(TransactionType.INCOME);
        testRequest.setCategory("Trabajo");
        testRequest.setTransactionDate(LocalDate.now());
    }

    @Test
    void create_shouldReturnTransactionResponse_whenValidRequest() {
        var savedTransaction = Transaction.builder()
                .id(1L)
                .description(testRequest.getDescription())
                .amount(testRequest.getAmount())
                .type(testRequest.getType())
                .category(testRequest.getCategory())
                .transactionDate(testRequest.getTransactionDate())
                .user(testUser)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        var response = transactionService.create(1L, testRequest);

        assertThat(response).isNotNull();
        assertThat(response.getDescription()).isEqualTo("Salario mensual");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getAmount()).isEqualByComparingTo("3500000");
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void getMonthlyBalance_shouldReturnSuperavit_whenIncomeExceedsExpense() {
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = start.plusMonths(1);

        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)))
                .thenReturn(new BigDecimal("5000000"));
        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)))
                .thenReturn(new BigDecimal("3000000"));

        var balance = transactionService.getMonthlyBalance(1L, 3, 2026);

        assertThat(balance.getStatus()).isEqualTo("SUPERAVIT");
        assertThat(balance.getBalance()).isEqualByComparingTo("2000000");
    }

    @Test
    void getMonthlyBalance_shouldReturnDeficit_whenExpenseExceedsIncome() {
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = start.plusMonths(1);

        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)))
                .thenReturn(new BigDecimal("1000000"));
        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)))
                .thenReturn(new BigDecimal("2500000"));

        var balance = transactionService.getMonthlyBalance(1L, 3, 2026);

        assertThat(balance.getStatus()).isEqualTo("DEFICIT");
        assertThat(balance.getBalance()).isNegative();
    }
}
