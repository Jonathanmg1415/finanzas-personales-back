package com.finanzas.app.service.impl;

import com.finanzas.app.domain.model.AuditLog;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.AuditLogRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.presentation.dto.response.AuditLogResponse;
import com.finanzas.app.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository     userRepository;

    /**
     * Registra un evento de auditoría.
     * Usa REQUIRES_NEW para que el log persista aunque la transacción padre falle.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entity,
                    Long entityId, String description, String ipAddress) {
        try {
            User user = userId != null
                    ? userRepository.findById(userId).orElse(null)
                    : null;

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entity(entity)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // El fallo del log nunca debe interrumpir la operación principal
            log.error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByUser(Long userId, int page, int size) {
        return auditLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}