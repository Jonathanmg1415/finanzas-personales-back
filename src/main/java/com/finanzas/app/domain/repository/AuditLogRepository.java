package com.finanzas.app.domain.repository;

import com.finanzas.app.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Todos los eventos de un usuario ordenados por fecha descendente
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Paginado para el endpoint de listado
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Filtrar por entidad
    List<AuditLog> findByUserIdAndEntityOrderByCreatedAtDesc(Long userId, String entity);
}