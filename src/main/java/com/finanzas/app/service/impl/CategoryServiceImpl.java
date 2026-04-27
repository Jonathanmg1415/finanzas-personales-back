package com.finanzas.app.service.impl;

import com.finanzas.app.domain.model.Category;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.CategoryRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.request.CategoryRequest;
import com.finanzas.app.presentation.dto.response.CategoryResponse;
import com.finanzas.app.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        // Verificar duplicado para este usuario
        categoryRepository.findByNameIgnoreCaseAndUserId(request.getName(), userId)
                .ifPresent(c -> { throw new IllegalArgumentException("Ya existe una categoría con ese nombre"); });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Category category = Category.builder()
                .name(request.getName().trim())
                .global(false)
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(Long userId) {
        return categoryRepository.findAvailableForUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .global(c.isGlobal())
                .build();
    }
}