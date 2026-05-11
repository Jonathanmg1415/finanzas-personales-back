package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.BudgetRequest;
import com.finanzas.app.presentation.dto.response.BudgetResponse;
import com.finanzas.app.presentation.exception.ErrorResponse;
import com.finanzas.app.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Presupuestos", description = "HU-23 — Definición y gestión de presupuestos mensuales por categoría. Solo puede existir un presupuesto activo por categoría por mes.")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "HU-23 — Crear presupuesto mensual",
        description = "Establece un límite de gasto para una categoría en un mes y año específico. El campo spent inicia en 0. Solo un presupuesto activo por categoría+mes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Presupuesto creado exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BudgetResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "category": "Alimentación",
                      "limitAmount": 400000,
                      "spent": 0,
                      "month": 4,
                      "year": 2026,
                      "message": "Presupuesto creado exitosamente"
                    }"""))),
        @ApiResponse(responseCode = "400", description = "Presupuesto duplicado o datos inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-05-11T10:00:00",
                      "status": 400,
                      "error": "Ya existe un presupuesto activo para esta categoría en el mes actual"
                    }"""))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(resolveUserId(userDetails), request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "HU-23 — Actualizar presupuesto activo",
        description = "Modifica el monto límite de un presupuesto existente. El campo spent no se resetea — conserva el acumulado de gastos del mes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Presupuesto actualizado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BudgetResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "category": "Alimentación",
                      "limitAmount": 500000,
                      "spent": 150000,
                      "month": 4,
                      "year": 2026,
                      "message": "Presupuesto actualizado exitosamente"
                    }"""))),
        @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permiso sobre este recurso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BudgetResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID del presupuesto", example = "1") @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(resolveUserId(userDetails), id, request));
    }

    @GetMapping
    @Operation(
        summary = "HU-23 — Listar presupuestos del mes",
        description = "Retorna todos los presupuestos activos del usuario para el mes y año indicados. El campo message no se incluye en el listado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de presupuestos del mes",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BudgetResponse.class),
                examples = @ExampleObject(value = """
                    [
                      {
                        "id": 1,
                        "category": "Alimentación",
                        "limitAmount": 500000,
                        "spent": 150000,
                        "month": 4,
                        "year": 2026
                      },
                      {
                        "id": 2,
                        "category": "Transporte",
                        "limitAmount": 150000,
                        "spent": 40000,
                        "month": 4,
                        "year": 2026
                      }
                    ]"""))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BudgetResponse>> findByMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Mes (1-12)", example = "4") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year) {
        return ResponseEntity.ok(budgetService.findByMonth(resolveUserId(userDetails), month, year));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}