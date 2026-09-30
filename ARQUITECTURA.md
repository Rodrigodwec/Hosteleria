# Arquitectura y razonamiento del proyecto

Este documento explica **cómo** está construida la aplicación y, sobre todo, **por qué** está construida así: qué problema resuelve cada pieza y qué decisión de diseño hay detrás. El [README.md](README.md) explica cómo instalar y ejecutar el proyecto; este documento explica su funcionamiento interno.

## Índice

1. [Visión de conjunto: cómo viaja una petición](#1-visión-de-conjunto-cómo-viaja-una-petición)
2. [El modelo de datos](#2-el-modelo-de-datos)
3. [Backend, capa por capa](#3-backend-capa-por-capa)
4. [Frontend, capa por capa](#4-frontend-capa-por-capa)
5. [Un caso de uso completo, de principio a fin](#5-un-caso-de-uso-completo-de-principio-a-fin)
6. [Decisiones de diseño y sus porqués](#6-decisiones-de-diseño-y-sus-porqués)

---

## 1. Visión de conjunto: cómo viaja una petición

La aplicación son dos proyectos independientes que solo se hablan por HTTP:

```
Angular (puerto 4200)  →  HTTP + JSON  →  Spring Boot (puerto 8081)  →  JDBC  →  MySQL
```

El backend no sabe nada de Angular ni genera HTML: es una **API REST pura** que solo entiende JSON. El frontend no sabe nada de Java: solo sabe hacer peticiones HTTP y pintar lo que recibe. Esta separación es deliberada — permite, por ejemplo, sustituir el frontend por una app móvil sin tocar una línea del backend.

Cuando el camarero pulsa un botón en Angular, la petición recorre siempre el mismo camino dentro del backend:

```
Petición HTTP
   ↓
JwtAuthenticationFilter   (¿quién eres? — lee el token)
   ↓
Spring Security           (¿tienes permiso para entrar aquí?)
   ↓
Controller                (¿qué endpoint es? recibe el JSON, lo valida)
   ↓
Service                   (lógica de negocio: reglas, permisos finos, cálculos)
   ↓
Repository                (traducir a SQL)
   ↓
MySQL
```

Y la respuesta hace el camino inverso, pero cambiando de forma: la base de datos devuelve **entidades** (`Usuario`, `Mesa`, `Comanda`...), y el Controller nunca las envía tal cual al navegador — las convierte antes a **DTOs**. Esa conversión es una de las decisiones más importantes del proyecto y se explica en la sección de `dto/`.

---

## 2. El modelo de datos

```
Usuario (camarero/admin)          Producto
   │  1                              │  1
   │                                 │
   │ N                               │ N
   ▼                                 ▼
 Mesa ──── 1 comanda activa ──── Comanda ──── 1:N ──── LineaComanda
   (vía camarero_id en Mesa)      (mesa_id,              (comanda_id,
                                   camarero_id)            producto_id)
```

- **`Usuario`** tiene un `Rol` (`ADMIN` o `CAMARERO`). No hay una tabla de roles aparte porque solo existen dos, fijos — un `@Enumerated(EnumType.STRING)` es suficiente y evita el sobrecoste de una tabla + join para algo que nunca va a tener más de dos valores.
- **`Mesa`** tiene un `camarero_id` opcional (`@ManyToOne` sin `optional=false`): una mesa libre no tiene camarero asignado, por eso el campo debe poder ser `null`.
- **`Comanda`** es el corazón del dominio: representa **una visita a una mesa**. Une mesa + camarero + líneas de producto. Cuando se cobra, no se borra — se marca `CERRADA` y guarda `fechaCierre`, para conservar histórico (aunque en esta versión de práctica no hay pantalla de histórico, el modelo ya lo soporta).
- **`LineaComanda`** guarda su propio `precioUnitario`, copiado del producto en el momento de añadirlo. Esto es intencional: si mañana el admin sube el precio de una cerveza, las comandas ya cobradas **no** deben cambiar de importe retroactivamente. Es la misma razón por la que un ticket de compra real nunca cambia de precio aunque la tienda suba tarifas al día siguiente.
- `Comanda.lineas` usa `cascade = CascadeType.ALL, orphanRemoval = true`: si se borra una comanda, sus líneas se borran solas (cascade), y si se quita una línea de la lista en memoria, JPA la borra de la base de datos sin que haya que hacerlo a mano (orphanRemoval). Ahorra un `lineaComandaRepository.delete(...)` explícito en el servicio.

---

## 3. Backend, capa por capa

### 3.1 `model/` — las entidades JPA

Cada clase es una tabla. Usan Lombok (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`) para no escribir a mano getters/setters/constructores — con 8 entidades, eso son cientos de líneas mecánicas que Lombok genera en tiempo de compilación.

Un detalle que vale la pena señalar: `LineaComanda.comanda` lleva `@JsonIgnore`. Sin eso, al serializar una `Comanda` a JSON, Jackson intentaría serializar también sus líneas, y cada línea intentaría serializar de vuelta su comanda — un bucle infinito (`StackOverflowError`). `@JsonIgnore` rompe ese ciclo cortando el lado "de vuelta" de la relación.

### 3.2 `repository/` — acceso a datos

Interfaces que extienden `JpaRepository<Entidad, TipoId>`. Con eso solo, Spring Data ya genera en tiempo de ejecución `save()`, `findById()`, `findAll()`, `deleteById()`, etc. — cero SQL escrito a mano para el CRUD básico.

Los métodos añadidos encima (`findByUsername`, `findByNumero`, `findByMesaIdAndEstado`...) se generan **a partir del nombre del método**: Spring Data parsea `findByMesaIdAndEstado` y construye automáticamente `WHERE mesa_id = ? AND estado = ?`. Esto evita escribir `@Query` para consultas simples; solo haría falta `@Query` con JPQL/SQL si la consulta fuera demasiado compleja para expresarse como nombre de método, cosa que no ocurre en este proyecto.

### 3.3 `security/` — quién eres y qué puedes hacer

Cuatro piezas que colaboran:

- **`UserPrincipal`**: envoltorio sobre `Usuario` que implementa `UserDetails`, la interfaz que Spring Security exige para representar "el usuario autenticado". Traduce el `Rol` de la entidad al formato que Spring Security espera (`ROLE_ADMIN`, `ROLE_CAMARERO` — el prefijo `ROLE_` es una convención fija del framework).
- **`CustomUserDetailsService`**: el punto donde Spring Security pregunta "¿quién es el usuario `admin`?". Busca en `UsuarioRepository` y devuelve un `UserPrincipal`. Es la pieza que conecta Spring Security con **nuestra** tabla de usuarios en lugar de una en memoria.
- **`JwtService`**: sabe generar y leer tokens JWT. Al generar el token mete `id`, `rol` y `nombre` como *claims* dentro del propio token — así el frontend puede leer quién ha iniciado sesión sin hacer una petición aparte. La clave de firma (`app.jwt.secret`) es simétrica (HMAC): la misma clave firma y verifica, así que **solo el backend** puede emitir tokens válidos, aunque cualquiera pueda leer su contenido (un JWT no es secreto, solo es infalsificable).
- **`JwtAuthenticationFilter`**: un filtro que se ejecuta en **todas** las peticiones, antes de llegar a ningún controlador. Busca la cabecera `Authorization: Bearer <token>`; si existe y es válido, "loguea" al usuario para esa petición metiéndolo en el `SecurityContextHolder`. Si no hay token, deja pasar la petición sin autenticar — será `SecurityConfig` quien decida si esa ruta necesitaba estar autenticada o no.

La razón de usar JWT en vez de sesiones de servidor: el backend es **stateless** (`SessionCreationPolicy.STATELESS` en `SecurityConfig`) — no guarda en memoria quién está logueado. Toda la identidad viaja dentro del propio token en cada petición. Esto simplifica escalar el backend a varias instancias en el futuro (no hace falta compartir sesión entre servidores) y es el patrón estándar para APIs consumidas por SPAs.

### 3.4 `config/` — arranque y configuración transversal

- **`SecurityConfig`**: define qué rutas son públicas (`/api/auth/**`, y las de Swagger) y cuáles exigen token (`anyRequest().authenticated()`), desactiva CSRF (solo tiene sentido en apps que usan cookies de sesión; con JWT en cabecera no aplica), configura CORS para aceptar peticiones solo desde `http://localhost:4200` (evita que cualquier página web pueda llamar a la API desde el navegador de un usuario), y engancha el `JwtAuthenticationFilter` **antes** del filtro estándar de usuario/contraseña de Spring Security.
- **`DataInitializer`**: implementa `CommandLineRunner`, que Spring ejecuta una vez justo después de arrancar. Comprueba `if (usuarioRepository.findByUsername(...).isEmpty())` antes de crear cada usuario — así es seguro reiniciar el backend cien veces sin duplicar usuarios de prueba.
- **`OpenApiConfig`**: define el esquema de seguridad Bearer/JWT para que Swagger UI muestre el botón "Authorize" y pueda probar endpoints protegidos desde la propia documentación.

### 3.5 `dto/` — el contrato con el exterior

Esta es una de las decisiones de diseño más importantes del proyecto: **los controladores nunca devuelven entidades JPA directamente**, siempre las convierten a un `record` (`UsuarioDto`, `MesaDto`, `ComandaDto`...) mediante un método estático `from(entidad)`.

¿Por qué no devolver la entidad tal cual, si total tiene los mismos campos? Varias razones, todas reales en este proyecto:

1. **Seguridad**: `Usuario` tiene un campo `password` (el hash bcrypt). Si se serializara la entidad directamente, ese hash viajaría al navegador en cada respuesta. `UsuarioDto` simplemente no tiene ese campo — es estructuralmente imposible filtrarlo por error.
2. **Evitar bucles de serialización**: ya visto en `LineaComanda ↔ Comanda`. Los DTOs aplanan la relación (`camareroNombre: String` en vez de un objeto `Usuario` completo), así que el problema ni se plantea.
3. **Forma de los datos adaptada al frontend, no a la base de datos**: `ComandaDto.total` no existe como columna en la tabla `comandas` — se calcula sumando los subtotales de las líneas en el propio `from(...)`. Es un dato derivado que tiene sentido en la respuesta HTTP pero no en el modelo relacional.
4. **Desacoplar el contrato público de la estructura interna**: se puede renombrar una columna o cambiar cómo se guarda un dato en la base de datos sin que eso rompa al frontend, siempre que el DTO se mantenga igual.

Los DTOs de **entrada** (`CrearMesaRequest`, `ProductoRequest`, `LoginRequest`...) llevan anotaciones de `jakarta.validation` (`@NotBlank`, `@Positive`, `@Size`...). Spring las valida automáticamente gracias a `@Valid` en la firma del método del controlador, **antes** de que el código del controlador se ejecute siquiera — si el JSON no cumple las reglas, Spring devuelve un 400 sin que el desarrollador tenga que escribir ningún `if`.

### 3.6 `service/` — donde vive la lógica de negocio

Los controladores son deliberadamente "tontos": reciben el JSON, delegan en el service, y envuelven el resultado en un DTO. Toda decisión de negocio vive en `service/`:

- **`MesaService.ocupar(...)`**: comprueba que la mesa no esté ya ocupada (`ApiException.conflict` si lo está) antes de crear la comanda y asignar el camarero. Esta comprobación no podría delegarse a una simple restricción de base de datos: es una regla de negocio ("una mesa ocupada no puede volver a ocuparse"), no una restricción estructural de los datos.
- **`ProductoService.listar(categoria)`**: si no llega categoría, devuelve todo (`findAll()`); si llega, filtra (`findByCategoria`). Es el método que resolvió el bug del filtrado de categorías del frontend — el controlador solo pasa el parámetro, la decisión de qué hacer con él vive aquí.
- **`UsuarioService.eliminar(...)`**: bloquea explícitamente borrar un usuario con rol `ADMIN`. Es una salvaguarda de negocio, no algo que la base de datos pueda impedir por sí sola.
- **`ComandaService`** es el más complejo, porque es donde vive la regla de permisos más importante de toda la aplicación:

  ```java
  private void verificarPermiso(Comanda comanda, UserPrincipal principal) {
      boolean esAdmin = principal.getUsuario().getRol() == Rol.ADMIN;
      boolean esPropietario = comanda.getCamarero().getId().equals(principal.getId());
      if (!esAdmin && !esPropietario) {
          throw ApiException.forbidden(...);
      }
  }
  ```

  Todos los métodos que modifican una comanda (`agregarLinea`, `actualizarCantidad`, `eliminarLinea`, `eliminarComanda`, `cobrar`) llaman a `verificarPermiso` nada más cargar la comanda, **antes** de tocar nada. Esta es la diferencia entre este proyecto y uno que solo oculta botones en el frontend: aunque alguien manipule la petición HTTP a mano (con curl, Postman, las devtools del navegador...) saltándose por completo el frontend, el backend sigue rechazando la operación. **La seguridad real vive en el backend; el frontend solo oculta opciones para que la interfaz tenga sentido, pero no es la barrera de seguridad.**

  Nótese que este método es privado y no lleva `@PreAuthorize`: no es un permiso "estático" que se pueda expresar con una anotación declarativa (`hasRole('ADMIN')`), porque depende de **datos concretos** (¿es esta comanda, de este camarero en particular, la que pertenece a quien hace la petición?). Ese tipo de permiso "dinámico" solo se puede comprobar con código, en tiempo de ejecución, una vez cargado el dato.

  En cambio, `MesaController.crear` y todo `ProductoController`/`UsuarioController` (salvo lectura) sí usan `@PreAuthorize("hasRole('ADMIN')")` directamente sobre el método del controlador, porque ahí el permiso **no depende de ningún dato**: o eres admin, o no lo eres, siempre, para ese endpoint entero. Es la herramienta correcta para cada tipo de regla: anotación declarativa para permisos fijos, código explícito para permisos que dependen de los datos.

### 3.7 `controller/` — la puerta de entrada HTTP

Cada controlador expone un recurso REST (`/api/mesas`, `/api/productos`, `/api/comandas`, `/api/usuarios`) siguiendo las convenciones HTTP: `GET` para leer, `POST` para crear, `PUT` para actualizar, `DELETE` para borrar. `@ResponseStatus(HttpStatus.CREATED)` en los `POST` de creación y `HttpStatus.NO_CONTENT` en los `DELETE` son también convención REST: un recurso creado responde `201`, no `200`; un borrado que no devuelve cuerpo responde `204`.

`@AuthenticationPrincipal UserPrincipal principal` es cómo el controlador obtiene "quién hace esta petición" sin tener que leer manualmente el token: Spring Security ya dejó el `UserPrincipal` en el contexto de seguridad (gracias al `JwtAuthenticationFilter`), y esta anotación simplemente lo inyecta como parámetro.

### 3.8 `exception/` — un único lugar para traducir errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) intercepta las excepciones lanzadas desde cualquier controlador o servicio, sin que cada método tenga que envolver su código en `try/catch`. Convierte:

- `ApiException` (la excepción "de negocio" propia del proyecto, con su `HttpStatus` ya decidido en el punto donde se lanza: `notFound`, `forbidden`, `conflict`, `badRequest`) → respuesta JSON con ese mismo código.
- `DataIntegrityViolationException` (la excepción que lanza Spring cuando MySQL rechaza una operación por una restricción de clave foránea) → `409 Conflict` con un mensaje legible.

Este último caso es interesante: sin este *handler*, intentar borrar un producto que ya está usado en alguna comanda provocaba una excepción sin capturar que Spring Security, en su capa de seguridad a nivel de método, transformaba en un `403` vacío y confuso — un bug real detectado y corregido durante el desarrollo de este proyecto. La lección de diseño es: **toda excepción que pueda ocurrir de forma esperable (violar una restricción de la base de datos es un caso totalmente previsible) merece su propio manejador explícito**, en vez de dejar que se propague sin control.

---

## 4. Frontend, capa por capa

### 4.1 `core/models/` — el contrato del backend, en TypeScript

Cada `.model.ts` es el espejo en TypeScript de los DTOs del backend (`Mesa` ≈ `MesaDto`, `Comanda` ≈ `ComandaDto`...). Los tipos union (`export type EstadoMesa = 'LIBRE' | 'OCUPADA'`) son el equivalente TypeScript de los enums de Java: como TypeScript no tiene enums "reales" con la misma semántica que Java y se compila a JavaScript plano, un tipo unión de strings literales es más ligero y se integra mejor con JSON (que no tiene concepto de enum, solo strings).

### 4.2 `core/services/` — la única capa que habla HTTP

Cada servicio (`MesaService`, `ComandaService`, `ProductoService`, `UsuarioService`) envuelve las llamadas a un recurso del backend y devuelve `Observable<T>`. Ningún componente hace `HttpClient.get(...)` directamente: siempre pasa por estos servicios. Esto centraliza la URL base (`environment.apiUrl`) y la forma exacta de cada petición en un solo sitio — si mañana cambia una ruta del backend, solo hay que tocar el servicio, no cada componente que lo usa.

### 4.3 `core/interceptors/jwt.interceptor.ts`

Un interceptor HTTP funcional (`HttpInterceptorFn`, el estilo moderno de Angular 18 con `inject()` en vez de clases con constructor) que se ejecuta automáticamente en **cada** petición saliente:

1. Añade la cabecera `Authorization: Bearer <token>` si hay sesión — así ningún servicio (`MesaService`, `ComandaService`...) necesita preocuparse de añadir el token a mano en cada llamada.
2. Si la respuesta es `401` (token caducado o inválido), fuerza `logout()` y redirige a `/login`. Es la contraparte en el frontend de que el backend sea stateless: si el backend deja de reconocer el token, el frontend debe reaccionar inmediatamente en vez de dejar la interfaz en un estado inconsistente (pantallas que fallan en silencio).

### 4.4 `core/guards/` — quién puede entrar a cada ruta

Tres *guards* (`CanActivateFn`, el estilo funcional moderno de Angular Router):

- **`authGuard`**: bloquea cualquier ruta protegida si no hay sesión → redirige a `/login`.
- **`adminGuard`**: además de estar logueado, exige `isAdmin()` → redirige a `/mesas` si un camarero intenta entrar a `/admin/...` tecleando la URL a mano.
- **`loginGuard`**: al revés — si ya hay sesión, no tiene sentido mostrar la pantalla de login, así que redirige directamente a `/mesas`.

Igual que ocurre en el backend con `verificarPermiso`, estos guards son **solo una conveniencia de interfaz** (evitan que un camarero vea por error una pantalla de admin), **no la barrera de seguridad real** — esa vive en el backend (`@PreAuthorize`), porque un guard de Angular se puede saltar trivialmente editando el JavaScript en las devtools del navegador. Frontend y backend aplican la misma regla de negocio por separado, con propósitos distintos: el guard cuida la experiencia de usuario, `@PreAuthorize`/`verificarPermiso` cuidan la seguridad real.

### 4.5 `app.routes.ts` y `app.config.ts` — el esqueleto de la aplicación

`app.routes.ts` define el árbol de rutas. Todas las páginas usan `loadComponent: () => import(...)` (*lazy loading*): el código de `AdminProductosComponent`, por ejemplo, ni siquiera se descarga en el navegador hasta que el usuario navega a `/admin/productos`. Esto mantiene pequeño el paquete inicial de JavaScript que carga un camarero, que nunca visitará las pantallas de administración.

Las rutas protegidas cuelgan de un nodo padre con `component: ShellComponent` — así la barra de navegación (el "shell") se renderiza una sola vez y las páginas hijas solo reemplazan el `<router-outlet>` interior, en vez de recargar toda la interfaz en cada navegación.

`app.config.ts` registra el interceptor JWT globalmente (`provideHttpClient(withInterceptors([jwtInterceptor]))`) — así se aplica automáticamente a cualquier petición hecha con `HttpClient` en toda la aplicación, sin tener que inyectarlo manualmente en cada servicio.

### 4.6 `layout/shell/` — la estructura visual común

`ShellComponent` es la barra superior (toolbar) que envuelve todas las pantallas protegidas. Su plantilla usa `@if (authService.isAdmin())` para mostrar los enlaces "Camareros" y "Menú" **solo** si el usuario es admin — de nuevo, una conveniencia visual, no una barrera de seguridad (la ruta detrás de esos enlaces ya está protegida por `adminGuard`, y el endpoint detrás de esa ruta ya está protegido por `@PreAuthorize`: tres capas independientes para la misma regla, cada una con su propósito).

### 4.7 `pages/` — cada pantalla

- **`login/`**: `FormBuilder` con `Validators.required` en usuario y contraseña. Usa `signal` para `loading` y `errorMessage` en vez de variables normales — Angular 18 usa *signals* como mecanismo reactivo moderno: cuando `loading.set(true)` cambia el valor, cualquier parte de la plantilla que lo use (`@if (loading())`) se actualiza sola, sin que haga falta `ChangeDetectorRef` ni RxJS para algo tan simple como un booleano de estado local.
- **`mesas/`**: pinta la rejilla de mesas. Al pulsar una mesa libre, abre `ConfirmDialogComponent` antes de ocuparla (evita ocupaciones accidentales); al pulsar una mesa ocupada, navega directamente a su detalle.
- **`mesa-detalle/`**: la pantalla más completa. El *getter* `puedeEditar` reproduce en el frontend, casi línea a línea, la misma regla que `ComandaService.verificarPermiso` en el backend (`comanda.camareroId === usuario.id || isAdmin()`). Se repite deliberadamente en ambos lados: en el frontend decide si mostrar los controles de edición (usabilidad); en el backend decide si aceptar o rechazar la petición (seguridad). `lineaSeleccionada` es un `signal` que recuerda qué línea de la comanda está "abierta" en el menú de sumar/restar/eliminar, compartido por ese único menú contextual en vez de tener un menú por línea.
- **`admin-camareros/`** y **`admin-productos/`**: siguen el mismo patrón — una tabla (`mat-table`) con un botón "nuevo" y acciones de editar/eliminar por fila, cada una abriendo su propio diálogo de formulario (`camarero-form-dialog`, `producto-form-dialog`) y refrescando la tabla (`cargar()`) al cerrarse con éxito. Separar el diálogo del componente de la tabla evita mezclar "cómo se listan los camareros" con "cómo se valida el formulario de un camarero" en la misma clase.

### 4.8 `shared/confirm-dialog/`

Un único componente de diálogo "¿Sí o no?" genérico, parametrizado por `data: ConfirmDialogData` (título, mensaje, texto de los botones). Se reutiliza en cuatro sitios distintos (ocupar mesa, eliminar comanda, cobrar mesa, eliminar camarero/producto) en vez de escribir cuatro diálogos casi idénticos — el criterio fue: en cuanto la tercera pantalla necesitó "¿seguro que quieres...?", ya no compensaba seguir copiando el mismo `mat-dialog` una vez más.

---

## 5. Un caso de uso completo, de principio a fin

**"Juan (camarero1) cobra la mesa 5."**

1. Angular: `MesaDetalleComponent.cobrarMesa()` abre `ConfirmDialogComponent`. Juan confirma.
2. `ComandaService.cobrar(comandaId)` hace `POST /api/comandas/42/cobrar` sin cuerpo.
3. `jwtInterceptor` añade `Authorization: Bearer <token de Juan>` a la petición antes de enviarla.
4. En el backend, `JwtAuthenticationFilter` lee el token, identifica a Juan y lo registra como usuario autenticado de esta petición.
5. `SecurityConfig` comprueba que `/api/comandas/**` no está en la lista de rutas públicas → exige estar autenticado. Juan lo está → pasa.
6. `ComandaController.cobrar(42, principal)` recibe la petición y delega en `ComandaService.cobrar(42, principal)`.
7. El servicio busca la comanda 42; si no existe, `ApiException.notFound` (→ `404` vía `GlobalExceptionHandler`).
8. `verificarPermiso`: ¿la comanda 42 es de Juan, o es Juan admin? Si la comanda fuera de Ana y Juan no fuera admin → `ApiException.forbidden` (→ `403`). Al ser la mesa de Juan, pasa.
9. Se marca `CERRADA`, se guarda `fechaCierre`, se libera la mesa (`EstadoMesa.LIBRE`, `camarero = null`).
10. El controlador envuelve la `Comanda` resultante en un `ComandaDto` (sin exponer la entidad ni el `password` de Juan, que ni siquiera está en esa relación) y la serializa a JSON.
11. Angular recibe la respuesta 200, navega de vuelta a `/mesas`.
12. `MesasComponent.cargarMesas()` vuelve a pedir `GET /api/mesas` y la mesa 5 aparece ahora como libre.

Cada paso de esta cadena corresponde exactamente a una de las capas descritas arriba — es la mejor forma de ver por qué existe cada una: si se quitara cualquiera de ellas, un paso concreto de este flujo dejaría de funcionar o dejaría de estar protegido.

---

## 6. Decisiones de diseño y sus porqués

| Decisión | Por qué |
|---|---|
| JWT stateless en vez de sesiones de servidor | El backend no guarda quién está logueado; toda la identidad viaja en el token. Más simple de escalar, patrón estándar para SPAs. |
| DTOs en vez de exponer entidades JPA | Evita filtrar el `password`, evita bucles de serialización, permite calcular campos derivados (`total`), desacopla el contrato HTTP de la estructura de la base de datos. |
| Permisos de "solo mi comanda" en el backend, no solo ocultos en el frontend | Un frontend siempre se puede saltar (curl, devtools). La seguridad real tiene que vivir donde no se puede manipular: el servidor. |
| `@PreAuthorize` para permisos fijos, código explícito (`verificarPermiso`) para permisos que dependen de datos | Una anotación declarativa no puede expresar "solo el dueño de este dato concreto"; eso solo se puede comprobar en tiempo de ejecución, tras cargar el dato. |
| `precioUnitario` copiado en cada `LineaComanda` en vez de referenciarlo siempre desde `Producto` | Una comanda ya cobrada no debe cambiar de importe si el precio del producto cambia después, igual que un ticket de compra real. |
| `GlobalExceptionHandler` centralizado | Un único sitio donde decidir cómo se traduce cada tipo de error a HTTP, en vez de `try/catch` repetido en cada controlador. |
| *Lazy loading* de cada página Angular | El navegador de un camarero nunca descarga el código de las pantallas de administración que nunca va a usar. |
| *Signals* en vez de variables sueltas para estado de componente | Angular actualiza la plantilla automáticamente cuando cambia un signal, sin gestión manual de detección de cambios. |
| Un único `ConfirmDialogComponent` reutilizable | Cuatro pantallas necesitaban "¿seguro que...?"; una sola implementación parametrizada evita duplicar el mismo diálogo cuatro veces. |
