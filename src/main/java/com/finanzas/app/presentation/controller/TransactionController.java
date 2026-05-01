package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.TransactionRequest;
import com.finanzas.app.presentation.dto.response.BalanceResponse;
import com.finanzas.app.presentation.dto.response.TransactionResponse;
import com.finanzas.app.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.finanzas.app.domain.enums.TransactionType;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Registro y consulta de ingresos y gastos")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Registrar una transacción")
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(resolveUserId(userDetails), request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las transacciones del usuario")
    public ResponseEntity<List<TransactionResponse>> findAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.findAllByUser(resolveUserId(userDetails)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una transacción por ID")
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id, resolveUserId(userDetails)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una transacción")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, resolveUserId(userDetails), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una transacción")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        transactionService.delete(id, resolveUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance")
    @Operation(summary = "Obtener balance mensual (ingresos - gastos)")
    public ResponseEntity<BalanceResponse> getMonthlyBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                transactionService.getMonthlyBalance(resolveUserId(userDetails), month, year));
    }

    @GetMapping("/category/expense")
    @Operation(summary = "Obtiene las transacciónes de tipo gasto que están asociadas a una categoría especifica")
    public ResponseEntity<List<TransactionResponse>> findCategoryExpenses(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long categoryId){
            return ResponseEntity.ok(
                transactionService.filterByTypeAndCategory(resolveUserId(userDetails), categoryId, TransactionType.EXPENSE)
            );
    }

    @GetMapping("/category/income")
    @Operation(summary = "Obtiene las transacciónes de tipo ingreso que están asociadas a una categoría especifica")
    public ResponseEntity<List<TransactionResponse>> findCategoryIncome(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long categoryId){
            return ResponseEntity.ok(
                transactionService.filterByTypeAndCategory(resolveUserId(userDetails), categoryId, TransactionType.INCOME)
            );
    }

    // Obtiene el userId desde el SecurityContext — sin tocar JwtUtil ni infrastructure
    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}
