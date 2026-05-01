package com.finanzas.app.domain.repository;

import com.finanzas.app.domain.enums.TransactionType;
import com.finanzas.app.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;                          // ← cambiado de LocalDate a LocalDateTime
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndCategoryIdAndType(Long userId, Long categoryId, TransactionType type);

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end); // ← LocalDateTime

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    BigDecimal sumByUserIdAndTypeAndPeriod(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,        // ← LocalDateTime
            @Param("endDate") LocalDateTime endDate);           // ← LocalDateTime
}