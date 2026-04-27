package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.BudgetRequest;
import com.finanzas.app.presentation.dto.response.BudgetResponse;
import com.finanzas.app.service.BudgetService;
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

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Presupuestos", description = "Definición y gestión de presupuestos mensuales por categoría")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crear un presupuesto mensual para una categoría")
    public ResponseEntity<BudgetResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(resolveUserId(userDetails), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar el monto de un presupuesto activo")
    public ResponseEntity<BudgetResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(resolveUserId(userDetails), id, request));
    }

    @GetMapping
    @Operation(summary = "Listar presupuestos del mes")
    public ResponseEntity<List<BudgetResponse>> findByMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(budgetService.findByMonth(resolveUserId(userDetails), month, year));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}