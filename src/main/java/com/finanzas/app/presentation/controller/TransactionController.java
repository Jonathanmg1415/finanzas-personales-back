package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.enums.TransactionType;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.TransactionRequest;
import com.finanzas.app.presentation.dto.response.BalanceResponse;
import com.finanzas.app.presentation.dto.response.TransactionResponse;
import com.finanzas.app.presentation.exception.ErrorResponse;
import com.finanzas.app.service.TransactionService;
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
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "HU-20 — Registro, consulta, actualización y eliminación de ingresos y gastos del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "Registrar una transacción",
        description = "Crea un nuevo ingreso (INCOME) o gasto (EXPENSE). El monto debe ser mayor a cero y la categoría es obligatoria. El balance del usuario se actualiza automáticamente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transacción registrada exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "description": "Supermercado Éxito",
                      "amount": 150000,
                      "type": "EXPENSE",
                      "categoryId": 1,
                      "categoryName": "Alimentación",
                      "transactionDate": "2026-04-01T10:00:00",
                      "notes": "Compras de la semana",
                      "createdAt": "2026-05-11T10:00:00"
                    }"""))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-05-11T10:00:00",
                      "status": 400,
                      "error": "Validación fallida",
                      "validationErrors": {
                        "amount": "El monto debe ser mayor a cero",
                        "categoryId": "Seleccionar una categoría es obligatorio"
                      }
                    }"""))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(resolveUserId(userDetails), request));
    }

    @GetMapping
    @Operation(
        summary = "Listar todas las transacciones",
        description = "Retorna todas las transacciones del usuario autenticado ordenadas por fecha descendente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de transacciones",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionResponse>> findAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.findAllByUser(resolveUserId(userDetails)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una transacción por ID",
        description = "Retorna una transacción específica. Solo accesible si pertenece al usuario autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacción encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permiso sobre este recurso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id, resolveUserId(userDetails)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una transacción",
        description = "Modifica todos los campos de una transacción existente. Solo el propietario puede actualizarla.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacción actualizada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permiso sobre este recurso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, resolveUserId(userDetails), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una transacción",
        description = "Elimina permanentemente una transacción. Solo el propietario puede eliminarla.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transacción eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permiso sobre este recurso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la transacción", example = "1") @PathVariable Long id) {
        transactionService.delete(id, resolveUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance")
    @Operation(summary = "Obtener balance mensual",
        description = "Calcula el balance del mes indicado: total ingresos - total gastos. Usa una query agregada en BD para eficiencia.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balance calculado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BalanceResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "totalIncome": 3500000,
                      "totalExpense": 650000,
                      "balance": 2850000
                    }"""))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BalanceResponse> getMonthlyBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Mes (1-12)", example = "4") @RequestParam int month,
            @Parameter(description = "Año", example = "2026") @RequestParam int year) {
        return ResponseEntity.ok(
                transactionService.getMonthlyBalance(resolveUserId(userDetails), month, year));
    }

    @GetMapping("/category/expense")
    @Operation(summary = "Gastos por categoría",
        description = "Filtra las transacciones de tipo EXPENSE asociadas a una categoría específica del usuario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de gastos de la categoría",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionResponse>> findCategoryExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la categoría", example = "1") @RequestParam Long categoryId) {
        return ResponseEntity.ok(
                transactionService.filterByTypeAndCategory(resolveUserId(userDetails), categoryId, TransactionType.EXPENSE));
    }

    @GetMapping("/category/income")
    @Operation(summary = "Ingresos por categoría",
        description = "Filtra las transacciones de tipo INCOME asociadas a una categoría específica del usuario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de ingresos de la categoría",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionResponse>> findCategoryIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID de la categoría", example = "1") @RequestParam Long categoryId) {
        return ResponseEntity.ok(
                transactionService.filterByTypeAndCategory(resolveUserId(userDetails), categoryId, TransactionType.INCOME));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}