# 💰 Personal Finance API

API REST para gestión financiera personal.
Proyecto académico — Arquitectura de Software | Sprint 1.

## Stack

- Java 17 + Spring Boot 3.2
- PostgreSQL (Render managed)
- Spring Security + JWT
- Springdoc OpenAPI (Swagger UI)
- Maven · Lombok · JUnit 5

## Arquitectura

Monolito con estilo estructural por capas:

```
presentation → service → domain → infrastructure
```

| Capa           | Responsabilidad                              |
|----------------|----------------------------------------------|
| presentation   | Controllers REST, DTOs, manejo de errores    |
| service        | Lógica de negocio y reglas financieras       |
| domain         | Entidades JPA e interfaces de repositorio    |
| infrastructure | Spring Security, JWT, configuración          |

## Endpoints

### Autenticación (público)
| Método | Ruta                    |
|--------|-------------------------|
| POST   | /api/v1/auth/register   |
| POST   | /api/v1/auth/login      |

### Transacciones (requiere JWT)
| Método | Ruta                              |
|--------|-----------------------------------|
| POST   | /api/v1/transactions              |
| GET    | /api/v1/transactions              |
| GET    | /api/v1/transactions/{id}         |
| PUT    | /api/v1/transactions/{id}         |
| DELETE | /api/v1/transactions/{id}         |
| GET    | /api/v1/transactions/balance      |

## Correr localmente

```bash
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE finanzas_db;"

# 2. Clonar el proyecto
git clone https://github.com/<usuario>/personal-finance-api.git
cd personal-finance-api

# 3. Ejecutar
mvn spring-boot:run
```

Swagger UI disponible en: http://localhost:8080/swagger-ui.html

## Variables de entorno (producción)

| Variable       | Descripción                          |
|----------------|--------------------------------------|
| DATABASE_URL   | URL PostgreSQL (inyectada por Render) |
| JWT_SECRET     | Clave JWT (generada por Render)      |
| PORT           | Puerto del servidor                  |

## Despliegue

El proyecto incluye `render.yaml` para despliegue automático.
Conectar el repositorio en Render y el servicio + BD se crean solos.