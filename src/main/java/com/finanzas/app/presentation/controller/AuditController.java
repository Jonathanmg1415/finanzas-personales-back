package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.response.AuditLogResponse;
import com.finanzas.app.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "HU-19 — Registro y consulta del historial de actividad del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService   auditService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(
        summary = "HU-19 — Consultar historial de actividad",
        description = "Retorna el registro paginado de todas las acciones realizadas por el usuario autenticado. " +
                      "Incluye creación, edición y eliminación de transacciones, presupuestos y categorías, así como eventos de login."
    )
    public ResponseEntity<Page<AuditLogResponse>> findActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                auditService.findByUser(resolveUserId(userDetails), page, size));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}