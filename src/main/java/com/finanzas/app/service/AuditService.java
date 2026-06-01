package com.finanzas.app.service;

import com.finanzas.app.presentation.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;

public interface AuditService {
    void log(Long userId, String action, String entity, Long entityId, String description, String ipAddress);
    Page<AuditLogResponse> findByUser(Long userId, int page, int size);
}