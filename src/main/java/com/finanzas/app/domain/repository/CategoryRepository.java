package com.finanzas.app.domain.repository;

import com.finanzas.app.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Categorías globales + las del usuario
    @Query("SELECT c FROM Category c WHERE c.global = true OR c.user.id = :userId")
    List<Category> findAvailableForUser(@Param("userId") Long userId);

    // Verificar duplicado por nombre para un usuario específico
    Optional<Category> findByNameIgnoreCaseAndUserId(String name, Long userId);

    // Solo las del usuario
    List<Category> findByUserId(Long userId);
}