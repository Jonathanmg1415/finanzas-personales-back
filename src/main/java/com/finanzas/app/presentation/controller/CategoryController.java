package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.CategoryRequest;
import com.finanzas.app.presentation.dto.response.CategoryResponse;
import com.finanzas.app.presentation.exception.ErrorResponse;
import com.finanzas.app.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "HU-39 — Gestión de categorías personalizadas. El listado combina las 8 categorías globales del sistema más las creadas por el usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "HU-39 — Crear categoría personalizada",
        description = "Crea una nueva categoría vinculada al usuario autenticado. El nombre debe ser único para ese usuario."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(value = "{ \"id\": 9, \"name\": \"Mascotas\", \"global\": false }"))),
        @ApiResponse(responseCode = "400", description = "Nombre duplicado o campo vacío",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(resolveUserId(userDetails), request));
    }

    @GetMapping
    @Operation(
        summary = "HU-39 — Listar categorías disponibles",
        description = "Retorna todas las categorías disponibles: las 8 globales predefinidas (global: true) más las del usuario (global: false)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de categorías",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "403", description = "Token inválido o expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CategoryResponse>> findAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(categoryService.findAll(resolveUserId(userDetails)));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow()
                .getId();
    }
}