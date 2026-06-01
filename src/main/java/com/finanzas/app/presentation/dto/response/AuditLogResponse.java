package com.finanzas.app.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entity;
    private Long entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}