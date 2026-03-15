package com.finanzas.app.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    @NotBlank @Email(message = "Email inválido")
    private String email;

    @NotBlank @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;
}
