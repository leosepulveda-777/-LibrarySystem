# 📚 LibrarySystem

Sistema completo de gestión de biblioteca — Spring Boot 3.x + PostgreSQL + JWT

## 🚀 Inicio rápido

```bash
git clone <repo>
cd LibrarySystem
docker-compose up --build
```

Accede a Swagger: **http://localhost:8080/swagger-ui.html**

## 🔑 Credenciales iniciales

| Rol | Email | Password |
|-----|-------|----------|
| ADMIN | admin@library.com | Admin123! |
| BIBLIOTECARIO | biblio@library.com | Biblio123! |

Para registrar un LECTOR usa `POST /api/v1/auth/register`.

## 🔐 Autenticación

1. Llama `POST /api/v1/auth/login` con email + password
2. Copia el `accessToken` de la respuesta
3. En Swagger, haz clic en **Authorize** (🔒) y pega el token
4. Todos los endpoints quedan autenticados

## 📋 Módulos disponibles

| Módulo | Base URL |
|--------|----------|
| Autenticación | `/api/v1/auth` |
| Catálogo | `/api/v1/catalogo` |
| Préstamos | `/api/v1/prestamos` |
| Reservas | `/api/v1/reservas` |
| Multas | `/api/v1/multas` |
| Lectores | `/api/v1/lectores` |
| Reportes | `/api/v1/reportes` |
| Recomendaciones | `/api/v1/recommendations/{lectorId}` |
| Notificaciones | `/api/v1/notifications` |

## 🛠️ Stack técnico

- Java 17 + Spring Boot 3.2.3
- Spring Security + JWT (access 15min, refresh 7días)
- Spring Data JPA + PostgreSQL 16
- Swagger UI (SpringDoc OpenAPI 2.3)
- Docker + Docker Compose

## 🧪 Tests

```bash
mvn test
```

## 🐳 Variables de entorno (docker-compose)

| Variable | Valor por defecto |
|----------|-------------------|
| DB_HOST | postgres |
| DB_PORT | 5432 |
| DB_NAME | library_db |
| DB_USERNAME | library_user |
| DB_PASSWORD | library_pass |
| JWT_SECRET | (ver docker-compose.yml) |
| SERVER_PORT | 8080 |

## 📂 Estructura del proyecto

```
src/main/java/com/library/
├── config/          # SecurityConfig, OpenApiConfig, LibraryProperties, DataInitializer
├── controller/      # 9 controladores REST
├── dto/
│   ├── request/     # 13 DTOs de entrada
│   └── response/    # 15 DTOs de salida
├── entity/          # 12 entidades JPA
├── enums/           # 6 enumeraciones
├── exception/       # 5 excepciones + GlobalExceptionHandler
├── repository/      # 12 repositorios JPA
├── scheduler/       # LibraryScheduler (tareas automáticas)
├── security/        # JwtService + JwtAuthFilter
└── service/impl/    # 9 servicios de negocio
```
