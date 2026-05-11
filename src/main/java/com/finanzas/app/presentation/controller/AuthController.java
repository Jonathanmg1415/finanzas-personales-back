package com.finanzas.app.presentation.controller;

import com.finanzas.app.presentation.dto.request.LoginRequest;
import com.finanzas.app.presentation.dto.request.RegisterRequest;
import com.finanzas.app.presentation.dto.response.AuthResponse;
import com.finanzas.app.presentation.exception.ErrorResponse;
import com.finanzas.app.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para registro e inicio de sesión. No requieren token JWT.")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    @Operation(
        summary = "HU-16 — Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario. La contraseña debe tener mínimo 8 caracteres, incluir mayúscula, minúscula, número y carácter especial. Retorna un token JWT válido por 24 horas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiJ9...",
                      "email": "juan@correo.com",
                      "fullName": "Juan Pérez",
                      "message": "Registro exitoso"
                    }"""))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o correo ya registrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "Correo duplicado", value = """
                        {
                          "timestamp": "2026-05-11T10:00:00",
                          "status": 400,
                          "error": "Ese correo ya tiene una cuenta asociada"
                        }"""),
                    @ExampleObject(name = "Contraseña débil", value = """
                        {
                          "timestamp": "2026-05-11T10:00:00",
                          "status": 400,
                          "error": "Validación fallida",
                          "validationErrors": {
                            "password": "La contraseña debe incluir mayúsculas, minúsculas, números y un carácter especial"
                          }
                        }""")
                }))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "HU-17 — Iniciar sesión",
        description = "Autentica al usuario con email y contraseña. Retorna un token JWT. Tras 3 intentos fallidos consecutivos, la cuenta se bloquea 30 minutos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso — token JWT generado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiJ9...",
                      "email": "juan@correo.com",
                      "fullName": "Juan Pérez"
                    }"""))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-05-11T10:00:00",
                      "status": 401,
                      "error": "Credenciales inválidas"
                    }"""))),
        @ApiResponse(responseCode = "429", description = "Cuenta bloqueada por múltiples intentos fallidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-05-11T10:00:00",
                      "status": 429,
                      "error": "Bloqueo temporal de 30 minutos por credenciales incorrectas"
                    }""")))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}