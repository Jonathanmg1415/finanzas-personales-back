package com.finanzas.app.presentation.controller;

import com.finanzas.app.infrastructure.security.JwtUtil;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Registro y consulta de ingresos y gastos")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Registrar una transacción")
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionRequest request) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las transacciones del usuario")
    public ResponseEntity<List<TransactionResponse>> findAll(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.findAllByUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una transacción por ID")
    public ResponseEntity<TransactionResponse> findById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.findById(id, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una transacción")
    public ResponseEntity<TransactionResponse> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una transacción")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = extractUserId(authHeader);
        transactionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance")
    @Operation(summary = "Obtener balance mensual (ingresos - gastos)")
    public ResponseEntity<BalanceResponse> getMonthlyBalance(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(transactionService.getMonthlyBalance(userId, month, year));
    }

    private Long extractUserId(String authHeader) {
        return jwtUtil.extractUserId(authHeader.substring(7));
    }
}
