# Pedidos360 — Plataforma Cloud-Native para Gestión de Pedidos

Proyecto semestral desarrollado para el curso **DSY1107 - Desarrollo Cloud Native I**.

Pedidos360 es una plataforma cloud-native para la gestión de pedidos de PyMEs, construida con una arquitectura de microservicios segura e integrada con identidad corporativa.

---

## Arquitectura

```
Angular (MSAL) → Microsoft Entra ID → Access Token JWT
→ AWS API Gateway (JWT Authorizer) → Spring Boot en EC2 → PostgreSQL en RDS
```

Los eventos de pedidos se publican de forma asíncrona en un cluster RabbitMQ de dos nodos:

```
Spring Boot (productor) → Exchange (direct / topic) → Colas → Consumidores
                                                       ↳ DLQ (mensajes rechazados)
Microservicio administrador → API REST → RabbitMQ (colas, exchanges y bindings)
```

### Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | Angular + MSAL |
| Identidad | Microsoft Entra ID (OAuth 2.0 / OIDC + PKCE) |
| API Gateway | AWS API Gateway (JWT Authorizer) |
| Backend | Java Spring Boot + Spring Security |
| Base de datos | PostgreSQL en AWS RDS |
| Mensajería | RabbitMQ (cluster de 2 nodos) + Spring AMQP |
| Administración de RabbitMQ | Microservicio Spring Boot (API REST) |
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

## Mensajería con RabbitMQ

### Exchanges

| Exchange | Tipo | Uso |
|----------|------|-----|
| `pedidos360.direct.exchange` | direct | Enruta por routing key exacta |
| `pedidos360.topic.exchange` | topic | Enruta por patrón (`pedido.#`) |
| `pedidos360.dlx.exchange` | direct | Dead-letter exchange: recibe los mensajes rechazados |

### Colas y bindings

| Cola | Exchange | Routing key | DLQ |
|------|----------|-------------|-----|
| `notificaciones.queue` | direct | `pedido.aceptado` | `notificaciones.dlq` |
| `despacho.queue` | direct | `pedido.despachado` | `despacho.dlq` |
| `auditoria.queue` | topic | `pedido.#` | `auditoria.dlq` |

Eventos publicados: `pedido.creado`, `pedido.aceptado`, `pedido.despachado` y `pedido.actualizado`. Los nombres de exchanges, colas y routing keys están centralizados en la clase `RabbitMQConfig`.

### Consumidores y manejo de errores

- Los consumidores usan `@RabbitListener` con confirmación manual (ACK).
- Si el mensaje se procesa bien, se confirma con `basicAck`.
- Si falla (por ejemplo, un mensaje sin `pedidoId` o `estado`), se rechaza con `basicNack` sin reencolar, queda registrado en el log y RabbitMQ lo envía a la DLQ correspondiente.
- Las DLQ no tienen consumidor, de modo que los mensajes fallidos quedan disponibles para su revisión.

### Políticas de retención

| Política | Aplica a | TTL | Máximo de mensajes |
|----------|----------|-----|--------------------|
| `retencion-colas` | `notificaciones.queue`, `despacho.queue`, `auditoria.queue` | 1 hora | 10.000 |
| `retencion-dlq` | colas `*.dlq` | 24 horas | 1.000 |

---

## Estructura del repositorio

```
ProyectoPedidos360/
├── FrontendPedidos360/        # Aplicación Angular
├── BackendPedidos360/         # API REST Spring Boot
├── AdminRabbitPedidos360/     # Microservicio administrador de RabbitMQ
├── RabbitMQCluster/           # Docker Compose del cluster RabbitMQ (2 nodos)
└── .github/                   # Configuración de GitHub
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

3. Indica dónde está RabbitMQ. Por defecto se usa `localhost:5672` con `guest / guest`. Para otro servidor, define las variables de entorno antes de ejecutar:
```bash
export SPRING_RABBITMQ_HOST=IP_DE_RABBITMQ
export SPRING_RABBITMQ_PORT=5672
export SPRING_RABBITMQ_USERNAME=TU_USUARIO_RABBIT
export SPRING_RABBITMQ_PASSWORD=TU_PASSWORD_RABBIT
```

4. Compila y ejecuta:
```bash
cd BackendPedidos360
./mvnw clean package -DskipTests
java -jar target/*.jar
```

---

### Cluster RabbitMQ en la nube (2 nodos con Docker Compose)

1. En la EC2 de RabbitMQ, con Docker y Docker Compose instalados, copia la carpeta `RabbitMQCluster` y crea el archivo `.env` a partir del ejemplo:
```bash
cd RabbitMQCluster
cp .env.example .env
```

2. Completa en `.env` los valores de `RABBITMQ_USER`, `RABBITMQ_PASS` y `ERLANG_COOKIE` (la cookie debe ser la misma para los dos nodos).

3. Levanta los dos nodos y verifica el cluster:
```bash
docker compose up -d
docker compose ps
docker exec rabbit1 rabbitmqctl cluster_status
```

4. Crea las políticas de retención:
```bash
docker exec rabbit1 rabbitmqctl set_policy retencion-colas "^(notificaciones|despacho|auditoria)\.queue$" '{"message-ttl":3600000,"max-length":10000}' --apply-to queues
docker exec rabbit1 rabbitmqctl set_policy retencion-dlq "\.dlq$" '{"message-ttl":86400000,"max-length":1000}' --apply-to queues
```

5. Panel de administración: `http://IP_DE_RABBITMQ:15672` (nodo 1) y `http://IP_DE_RABBITMQ:15673` (nodo 2). Habilita esos puertos y el 5672 en el security group solo para las IP que lo necesiten.

---

### Backend en local (sin AWS ni Azure)

1. Levanta RabbitMQ y PostgreSQL:
```bash
cd BackendPedidos360
docker compose up -d
```

2. Ejecuta con el perfil `local`:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

3. Tokens de prueba (en vez del JWT de Entra ID): `admin`, `operador`, `cliente`
```bash
curl -H "Authorization: Bearer operador" http://localhost:8080/api/orders
```

Panel de RabbitMQ: `http://localhost:15672` (guest / guest)

### Microservicio administrador de RabbitMQ (local)

```bash
cd AdminRabbitPedidos360
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

Corre en `http://localhost:8081` y requiere el token `admin`:

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/admin/queues` | Crear cola (opcional: `durable`, `deadLetterExchange`, `deadLetterRoutingKey`, `ttlMs`, `maxLength`) |
| GET | `/api/admin/queues/{name}` | Ver mensajes y consumidores de una cola |
| DELETE | `/api/admin/queues/{name}` | Eliminar cola |
| POST | `/api/admin/exchanges` | Crear exchange (`direct`, `topic` o `fanout`) |
| DELETE | `/api/admin/exchanges/{name}` | Eliminar exchange |
| POST | `/api/admin/bindings` | Crear binding (`queue`, `exchange`, `routingKey`) |
| DELETE | `/api/admin/bindings` | Eliminar binding |

### Microservicio administrador de RabbitMQ (nube)

1. Crea `application.properties` a partir del ejemplo:
```bash
cd AdminRabbitPedidos360/src/main/resources
cp application.properties.example application.properties
```

2. Completa el emisor de tokens de Entra ID:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/TU_TENANT_ID/v2.0
```

3. Compila y copia el jar a la EC2 del backend:
```bash
cd AdminRabbitPedidos360
./mvnw clean package -DskipTests
```

4. En la EC2, define el acceso a RabbitMQ y ejecútalo:
```bash
export SPRING_RABBITMQ_HOST=IP_PRIVADA_DE_RABBITMQ
export SPRING_RABBITMQ_PORT=5672
export SPRING_RABBITMQ_USERNAME=TU_USUARIO_RABBIT
export SPRING_RABBITMQ_PASSWORD=TU_PASSWORD_RABBIT
nohup java -jar AdminRabbitPedidos360-0.0.1-SNAPSHOT.jar > admin.log 2>&1 &
```

5. Llama a la API con el token de un usuario con rol `Admin` (el puerto 8081 no se expone públicamente):
```bash
curl -X POST localhost:8081/api/admin/queues -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"prueba.queue","ttlMs":60000,"maxLength":100}'
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
- El microservicio administrador de RabbitMQ solo acepta tokens con rol `Admin`; otros roles reciben `403 Forbidden`
