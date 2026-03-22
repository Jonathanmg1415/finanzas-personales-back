package com.finanzas.app.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "Correo inválido: formato incorrecto")
    private String email;

  
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
        message = "La contraseña debe incluir mayúsculas, minúsculas, números y un carácter especial"
    )
    private String password;
}