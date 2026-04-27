package com.finanzas.app.service;

import com.finanzas.app.presentation.dto.request.CategoryRequest;
import com.finanzas.app.presentation.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(Long userId, CategoryRequest request);
    List<CategoryResponse> findAll(Long userId);
}