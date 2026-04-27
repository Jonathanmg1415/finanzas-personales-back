package com.finanzas.app.presentation.controller;

import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.CategoryRequest;
import com.finanzas.app.presentation.dto.response.CategoryResponse;
import com.finanzas.app.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías personalizadas")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crear una categoría personalizada")
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(resolveUserId(userDetails), request));
    }

    @GetMapping
    @Operation(summary = "Listar categorías disponibles (globales + propias)")
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