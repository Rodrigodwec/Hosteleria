# 🍽️ Hostelería App

Aplicación web full-stack de gestión de un restaurante/bar. Permite a los **camareros** llevar las mesas y las comandas en tiempo real, y a los **administradores** gestionar el personal y la carta. Desarrollada como proyecto de práctica con **Spring Boot** (backend) y **Angular** (frontend).

## 📋 Índice

- [Características](#-características)
- [Tecnologías](#️-tecnologías)
- [Arquitectura](#️-arquitectura)
- [Requisitos previos](#-requisitos-previos)
- [Instalación y puesta en marcha](#-instalación-y-puesta-en-marcha)
- [Usuarios de prueba](#-usuarios-de-prueba)
- [Roles y permisos](#-roles-y-permisos)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Principales endpoints de la API](#-principales-endpoints-de-la-api)
- [Notas de desarrollo](#-notas-de-desarrollo)

## ✨ Características

- Autenticación mediante **JWT** (sin sesiones, stateless)
- Dos roles con permisos diferenciados: **Administrador** y **Camarero**
- Vista de mesas con estado visual en tiempo real (libre / ocupada)
- Al ocupar una mesa se abre una comanda asociada al camarero que la inicia
- Carta organizada por categorías (Comida / Bebida / Postre)
- Gestión de la comanda: añadir productos, sumar/restar unidades, eliminar líneas
- Cobro de mesa (cierra la comanda y la libera) y eliminación de comanda completa
- **Un camarero solo puede modificar las comandas que él mismo ha abierto**; un administrador puede modificar cualquiera
- Panel de administración: alta/baja/edición de camareros y CRUD completo del menú

## 🛠️ Tecnologías

**Backend**
- Java 17
- Spring Boot 4.1
- Spring Security + JWT ([jjwt](https://github.com/jwtk/jjwt))
- Spring Data JPA / Hibernate
- MySQL 8
- Maven (incluye wrapper `mvnw`)

**Frontend**
- Angular 18 (componentes *standalone*, signals)
- Angular Material
- TypeScript
- RxJS

## 🏗️ Arquitectura

```
hosteleriaPractica/
├── backendpractica/     # API REST (Spring Boot)
└── frontendpractica/    # SPA (Angular)
```

El frontend consume la API REST del backend por HTTP. Cada petición autenticada incluye el JWT obtenido en el login en la cabecera `Authorization: Bearer <token>`.

## ✅ Requisitos previos

- [JDK 17](https://learn.microsoft.com/java/openjdk/download)
- [Node.js](https://nodejs.org/) 18 o superior (incluye npm)
- [MySQL](https://dev.mysql.com/downloads/mysql/) 8
- No hace falta instalar Maven ni Angular CLI de forma global: el proyecto incluye el *wrapper* de Maven (`mvnw`) y se usa `npx` para Angular CLI

## 🚀 Instalación y puesta en marcha

### 1. Clonar el repositorio

```bash
git clone https://github.com/Rodrigodwec/Hosteleria.git
cd Hosteleria
```

### 2. Configurar la base de datos

Crea la base de datos y un usuario dedicado en MySQL:

```sql
CREATE DATABASE IF NOT EXISTS hosteleria_practica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'practica'@'localhost' IDENTIFIED BY 'practica_pw123';
GRANT ALL PRIVILEGES ON hosteleria_practica.* TO 'practica'@'localhost';
FLUSH PRIVILEGES;
```

> Si prefieres otras credenciales, ajústalas en `backendpractica/src/main/resources/application.properties`.

### 3. Arrancar el backend

```bash
cd backendpractica
./mvnw spring-boot:run
```

Queda escuchando en **http://localhost:8081**. Al arrancar por primera vez crea automáticamente el esquema de la base de datos y los usuarios de prueba.

### 4. Arrancar el frontend

En otra terminal:

```bash
cd frontendpractica
npm install
npx ng serve
```

Queda disponible en **http://localhost:4200**.

### 5. Acceder a la aplicación

Abre [http://localhost:4200](http://localhost:4200) en el navegador e inicia sesión con cualquiera de los usuarios de prueba.

## 👤 Usuarios de prueba

| Usuario | Contraseña | Rol | Nombre |
|---|---|---|---|
| `admin` | `admin123` | Administrador | Administrador |
| `camarero1` | `camarero123` | Camarero | Juan Gómez |
| `camarero2` | `camarero123` | Camarero | Ana López |

Se crean automáticamente la primera vez que arranca el backend (si no existen ya en la base de datos).

## 🔐 Roles y permisos

| Acción | Camarero | Administrador |
|---|:---:|:---:|
| Ver todas las mesas | ✅ | ✅ |
| Ocupar una mesa libre | ✅ | ✅ |
| Modificar una comanda propia | ✅ | ✅ |
| Modificar una comanda de otro camarero | ❌ | ✅ |
| Cobrar / eliminar una comanda (propia) | ✅ | ✅ |
| Crear, editar y eliminar mesas | ❌ | ✅ |
| Gestionar el menú (productos) | ❌ | ✅ |
| Gestionar camareros | ❌ | ✅ |

La restricción de "solo la comanda propia" se aplica en el backend (no solo ocultando botones en el frontend), comparando el usuario autenticado con el camarero asignado a la comanda.

## 📁 Estructura del proyecto

**Backend** — `backendpractica/src/main/java/.../backendpractica`

```
config/       → configuración de seguridad y carga de datos iniciales
controller/   → endpoints REST
dto/          → objetos de transferencia de datos (peticiones y respuestas)
exception/    → manejo centralizado de errores
model/        → entidades JPA
repository/   → acceso a datos (Spring Data JPA)
security/     → JWT, filtro de autenticación, UserDetails
service/      → lógica de negocio y reglas de permisos
```

**Frontend** — `frontendpractica/src/app`

```
core/         → modelos, servicios HTTP, guards e interceptor (transversal a toda la app)
layout/       → barra de navegación y estructura general
pages/        → pantallas: login, mesas, detalle de mesa, administración
shared/       → componentes reutilizables (p. ej. diálogo de confirmación)
```

## 🔌 Principales endpoints de la API

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `POST` | `/api/auth/login` | Inicia sesión y devuelve un JWT | Público |
| `GET` | `/api/mesas` | Lista todas las mesas | Autenticado |
| `POST` | `/api/mesas/{id}/ocupar` | Ocupa una mesa libre y abre una comanda | Autenticado |
| `GET` | `/api/productos?categoria=` | Lista la carta, filtrable por categoría | Autenticado |
| `GET` | `/api/comandas/mesa/{mesaId}` | Obtiene la comanda activa de una mesa | Autenticado |
| `POST` | `/api/comandas/{id}/lineas` | Añade un producto a la comanda | Dueño de la comanda o Admin |
| `PUT` | `/api/comandas/{id}/lineas/{lineaId}` | Cambia la cantidad de una línea | Dueño de la comanda o Admin |
| `DELETE` | `/api/comandas/{id}/lineas/{lineaId}` | Elimina una línea de la comanda | Dueño de la comanda o Admin |
| `DELETE` | `/api/comandas/{id}` | Elimina la comanda y libera la mesa | Dueño de la comanda o Admin |
| `POST` | `/api/comandas/{id}/cobrar` | Cierra la comanda y libera la mesa | Dueño de la comanda o Admin |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/usuarios` | CRUD de camareros | Solo Admin |
| `GET` / `POST` / `PUT` / `DELETE` | `/api/productos` | CRUD del menú (`GET` es público para autenticados) | Solo Admin (excepto `GET`) |

## 📝 Notas de desarrollo

- El backend se ha desarrollado en **Eclipse** y el frontend en **VS Code**.
- Si importas el backend en Eclipse, instala el integrador de **Lombok** para tu instalación de Eclipse (ejecuta el `.jar` de Lombok desde las dependencias de Maven del proyecto y sigue el instalador) — sin él, Eclipse no reconoce los métodos generados por anotaciones como `@Data` o `@Builder`.
- `spring.jpa.hibernate.ddl-auto=update` genera y actualiza el esquema de la base de datos automáticamente; no se necesita ejecutar ningún script SQL manualmente.
