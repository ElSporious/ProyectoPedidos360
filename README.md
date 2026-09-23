# Pedidos360 — Plataforma Cloud-Native para Gestión de Pedidos

Proyecto semestral desarrollado para el curso **DSY1107 - Desarrollo Cloud Native I**.

Pedidos360 es una plataforma cloud-native para la gestión de pedidos de PyMEs, construida con una arquitectura de microservicios segura e integrada con identidad corporativa.

---

## Arquitectura

```
Angular (MSAL) → Microsoft Entra ID → Access Token JWT
→ AWS API Gateway (JWT Authorizer) → Spring Boot en EC2 → PostgreSQL en RDS
```

### Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | Angular + MSAL |
| Identidad | Microsoft Entra ID (OAuth 2.0 / OIDC + PKCE) |
| API Gateway | AWS API Gateway (JWT Authorizer) |
| Backend | Java Spring Boot + Spring Security |
| Base de datos | PostgreSQL en AWS RDS |
| Despliegue | AWS EC2 + Docker |

---

## Módulos funcionales

### Gestión de Pedidos
- CRUD de pedidos con flujo de estados: `CREADO → ACEPTADO → EN_PREPARACIÓN → DESPACHADO → ENTREGADO` (o `CANCELADO`)
- Regla de negocio: no se puede pasar a `DESPACHADO` sin haber sido `ACEPTADO` antes

### Catálogo de Productos y Stock
- CRUD de productos con precio y stock
- Regla de negocio: al aceptar un pedido, el stock disminuye según los productos del pedido

---

## Roles y permisos

| Acción | Admin | Operador | Cliente |
|--------|-------|----------|---------|
| Ver catálogo | ✅ | ✅ | ✅ |
| Crear pedido | — | ✅ | ✅ |
| Ver pedidos propios | ✅ | ✅ | ✅ |
| Ver todos los pedidos | ✅ | ✅ | ❌ |
| Cambiar estado operacional | — | ✅ | ❌ |
| Crear/editar producto | ✅ | ❌ | ❌ |
| Administrar stock | ✅ | ❌ | ❌ |

---

## Estructura del repositorio

```
ProyectoPedidos360/
├── FrontendPedidos360/        # Aplicación Angular
└── BackendPedidos360/         # API REST Spring Boot
```

---

## Cómo ejecutar el proyecto

### Requisitos previos
- Node.js 18+
- Java 21+
- Maven
- Cuenta en Microsoft Entra ID con la aplicación registrada

### Frontend

```bash
cd FrontendPedidos360
npm install
ng serve
```

La aplicación estará disponible en `http://localhost:4200`

### Backend

1. Copia el archivo de configuración de ejemplo:
```bash
cd BackendPedidos360/src/main/resources
cp application.properties.example application.properties
```

2. Completa los valores en `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://HOST:5432/pedidos360?sslmode=require
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/TU_TENANT_ID/v2.0
spring.security.oauth2.resourceserver.jwt.audiences=TU_CLIENT_ID
```

3. Compila y ejecuta:
```bash
cd BackendPedidos360
./mvnw clean package -DskipTests
java -jar target/*.jar
```

---

## Endpoints principales

### Pedidos
| Método | Ruta | Descripción | Rol requerido |
|--------|------|-------------|---------------|
| POST | `/api/orders` | Crear pedido | Cliente, Operador |
| GET | `/api/orders` | Listar pedidos | Admin, Operador, Cliente |
| GET | `/api/orders/{id}` | Detalle de pedido | Admin, Operador, Cliente |
| PUT | `/api/orders/{id}/status` | Cambiar estado | Operador |

### Catálogo
| Método | Ruta | Descripción | Rol requerido |
|--------|------|-------------|---------------|
| GET | `/api/catalog/products` | Listar productos | Admin, Operador, Cliente |
| POST | `/api/catalog/products` | Crear producto | Admin |
| PUT | `/api/catalog/products/{id}` | Editar producto | Admin |

---

## Seguridad

- Autenticación mediante **Microsoft Entra ID** con flujo **Authorization Code + PKCE**
- Tokens JWT validados en **AWS API Gateway** y en **Spring Boot**
- Solicitudes sin token → `401 Unauthorized`
- Solicitudes con token válido pero sin permiso → `403 Forbidden`
- Roles asignados en Entra ID y extraídos del claim `roles` del JWT
