package com.finanzas.app.service;

import com.finanzas.app.domain.enums.TransactionType;
import com.finanzas.app.domain.model.Transaction;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.model.Category;
import com.finanzas.app.domain.repository.BudgetRepository;
import com.finanzas.app.domain.repository.CategoryRepository;
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
import java.time.LocalDateTime;              // ← cambiado de LocalDate a LocalDateTime
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private TransactionServiceImpl transactionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@finanzas.com")
                .fullName("Usuario Test")
                .build();
    }

    // ─── HU-20: Registro de Gastos ───────────────────────────────────────────

    @Test
    void create_shouldRegisterExpense_whenDataIsValid() {
        var category = Category.builder().id(1L).name("Alimentación").global(true).build();
        var request = buildExpenseRequest("Almuerzo", new BigDecimal("25000"),
                1L, null);

        var saved = buildTransaction(1L, request, category);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(saved);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                any(), any(), anyInt(), anyInt())).thenReturn(Optional.empty());

        var response = transactionService.create(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.getAmount()).isEqualByComparingTo("25000");
        assertThat(response.getCategoryName()).isEqualTo("Alimentación");
    }

    @Test
    void create_shouldStoreNote_whenOptionalNoteIsProvided() {
        var category = Category.builder().id(1L).name("Transporte").global(true).build();
        var request = buildExpenseRequest("Taxi", new BigDecimal("15000"),
                1L, "Viaje al aeropuerto");

        var saved = buildTransaction(2L, request, category);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(saved);
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                any(), any(), anyInt(), anyInt())).thenReturn(Optional.empty());

        var response = transactionService.create(1L, request);

        assertThat(response.getNotes()).isEqualTo("Viaje al aeropuerto");
    }

    @Test
    void getMonthlyBalance_shouldReduceBalance_whenExpenseIsRegistered() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);  // ← LocalDateTime
        LocalDateTime end = start.plusMonths(1);

        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)))
                .thenReturn(new BigDecimal("5000000"));
        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)))
                .thenReturn(new BigDecimal("3000000"));

        var balance = transactionService.getMonthlyBalance(1L, 3, 2026);

        assertThat(balance.getBalance()).isEqualByComparingTo("2000000");
        assertThat(balance.getStatus()).isEqualTo("SUPERAVIT");
    }

    @Test
    void getMonthlyBalance_shouldReturnSuperavit_whenIncomeExceedsExpense() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);  // ← LocalDateTime
        LocalDateTime end = start.plusMonths(1);

        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)))
                .thenReturn(new BigDecimal("5000000"));
        when(transactionRepository.sumByUserIdAndTypeAndPeriod(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)))
                .thenReturn(new BigDecimal("3000000"));

        var balance = transactionService.getMonthlyBalance(1L, 3, 2026);

        assertThat(balance.getStatus()).isEqualTo("SUPERAVIT");
    }

    @Test
    void getMonthlyBalance_shouldReturnDeficit_whenExpenseExceedsIncome() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);  // ← LocalDateTime
        LocalDateTime end = start.plusMonths(1);

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

    // ─── helpers ─────────────────────────────────────────────────────────────

    private TransactionRequest buildExpenseRequest(String desc, BigDecimal amount,
                                                    Long categoryId, String notes) {
        var req = new TransactionRequest();
        req.setDescription(desc);
        req.setAmount(amount);
        req.setType(TransactionType.EXPENSE);
        req.setCategoryId(categoryId);
        req.setTransactionDate(LocalDateTime.now());  // ← LocalDateTime
        req.setNotes(notes);
        return req;
    }

    private Transaction buildTransaction(Long id, TransactionRequest req, Category category) {
        return Transaction.builder()
                .id(id)
                .description(req.getDescription())
                .amount(req.getAmount())
                .type(req.getType())
                .category(category)
                .transactionDate(req.getTransactionDate())
                .notes(req.getNotes())
                .user(testUser)
                .build();
    }
}