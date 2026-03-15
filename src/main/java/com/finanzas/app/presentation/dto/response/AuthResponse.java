package com.finanzas.app.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
}
