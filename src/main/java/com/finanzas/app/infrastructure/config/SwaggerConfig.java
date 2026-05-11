package com.finanzas.app.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Finanzas Personales API",
        version = "2.0",
        description = """
            API REST para gestión financiera personal — Caso 12
            
            Permite a los usuarios registrar y analizar ingresos, gastos y presupuestos mensuales.
            
            **Historias de usuario implementadas:**
            - HU-16: Registro de usuario con política de contraseña
            - HU-17: Login con bloqueo temporal por intentos fallidos
            - HU-20: CRUD de transacciones + balance mensual + filtro por categoría
            - HU-39: Gestión de categorías personalizadas
            - HU-23: Definición de presupuestos mensuales por categoría
            
            **Autenticación:** Bearer JWT — obtener el token con POST /api/v1/auth/login y usarlo en el botón Authorize 🔒.
            """,
        contact = @Contact(
            name = "Jonathan Marin García",
            email = "jonathan.marin@udea.edu.co"
        ),
        license = @License(name = "Universidad de Antioquia — Arquitectura de Software 2026-1")
    ),
    servers = {
        @Server(url = "https://finanzas-personales-back.onrender.com", description = "Producción (Render)"),
        @Server(url = "http://localhost:8080", description = "Desarrollo local")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Token JWT obtenido en POST /api/v1/auth/login. Válido por 24 horas."
)
public class SwaggerConfig {
}