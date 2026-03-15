# Personal Finance API

API REST para gestión financiera personal desarrollada con Spring Boot.

**Curso:** Arquitectura de Software  
**Sprint 1** | Arquitectura por Capas (Layered Architecture) | Despliegue en Render

---

## Arquitectura

Este proyecto implementa una **arquitectura monolítica con estilo estructural por capas**, organizada en cuatro niveles con dependencias unidireccionales de arriba hacia abajo:

```
Presentation  →  Service  →  Domain/Repository  →  Infrastructure
```

| Capa | Responsabilidad |
|------|----------------|
| `presentation` | Controllers REST, DTOs, mappers, manejo de errores HTTP |
| `service` | Lógica de negocio, reglas financieras, validaciones |
| `domain` | Entidades JPA, interfaces de repositorio, enums |
| `infrastructure` | Spring Security, JWT, configuración, Postgres |

---

## Tecnologías

- Java 17 + Spring Boot 3.2
- PostgreSQL (Render managed)
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA / Hibernate
- Springdoc OpenAPI (Swagger UI)
- Lombok
- JUnit 5 + Mockito

---

## Endpoints principales

### Autenticación (público)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión → retorna JWT |

### Transacciones (requiere JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/transactions` | Crear transacción |
| GET | `/api/v1/transactions` | Listar transacciones del usuario |
| GET | `/api/v1/transactions/{id}` | Obtener por ID |
| PUT | `/api/v1/transactions/{id}` | Actualizar |
| DELETE | `/api/v1/transactions/{id}` | Eliminar |
| GET | `/api/v1/transactions/balance?month=3&year=2026` | Balance mensual |

---

## Ejecución local

### Prerrequisitos
- Java 17+
- Maven 3.8+
- PostgreSQL corriendo en `localhost:5432`

### Pasos

```bash
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE finanzas_db;"

# 2. Clonar y entrar al proyecto
git clone https://github.com/<tu-usuario>/personal-finance-api.git
cd personal-finance-api

# 3. Ejecutar
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`  
Swagger UI en `http://localhost:8080/swagger-ui.html`

---

## Variables de entorno en producción (Render)

| Variable | Descripción |
|----------|-------------|
| `DATABASE_URL` | URL de conexión PostgreSQL (inyectada por Render) |
| `JWT_SECRET` | Clave secreta para firmar tokens (generada por Render) |
| `PORT` | Puerto del servidor (inyectado por Render) |

---

## Despliegue en Render

1. Hacer push del proyecto a GitHub
2. En Render: **New Web Service** → conectar repositorio
3. Render detecta el `render.yaml` automáticamente y crea el servicio + base de datos
4. El deploy se activa en cada push a `main`

---

## Estructura del proyecto

```
src/main/java/com/finanzas/app/
├── presentation/
│   ├── controller/       # REST Controllers
│   ├── dto/              # Request / Response
│   └── exception/        # GlobalExceptionHandler
├── service/
│   ├── TransactionService.java  (interfaz)
│   └── impl/             # Implementaciones
├── domain/
│   ├── model/            # Entidades JPA
│   ├── repository/       # Interfaces JpaRepository
│   └── enums/
└── infrastructure/
    ├── config/           # Security, CORS, Swagger
    └── security/         # JWT Filter + Util
```
