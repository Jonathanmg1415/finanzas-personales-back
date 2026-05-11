package com.finanzas.app.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos requeridos para registrar un nuevo usuario")
public class RegisterRequest {

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    @Schema(description = "Correo electrónico — debe ser único en el sistema", example = "juan@correo.com")
    @NotBlank(message = "El correo es requerido")
    @Email(message = "Correo inválido: formato incorrecto")
    private String email;

    @Schema(description = "Contraseña: mínimo 8 caracteres, debe incluir mayúscula, minúscula, número y carácter especial", example = "Segura123!")
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
        message = "La contraseña debe incluir mayúsculas, minúsculas, números y un carácter especial"
    )
    private String password;
}