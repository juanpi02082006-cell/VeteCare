# Plan técnico — VetCare Santander

**Versión:** 1.0 · **Fecha:** 2026-11-17 · **Puerta 2:** revisión de los Developers

## 1. Tecnología

Java 21 · Spring Boot 4.1 · Spring Data JPA (Hibernate) · Spring Security (BCrypt) · Thymeleaf + `thymeleaf-extras-springsecurity6`
· Bootstrap 5.3 y Bootstrap Icons (CDN) · H2 en desarrollo y MySQL en la nube · JUnit 5 + MockMvc + Spring Security Test.
Sprint 3: Chart.js, `RestClient` (TheDogAPI y TheCatAPI), Apache POI, OpenPDF, ZXing, Docker y Render.

## 2. Arquitectura MVC sin capa service

```
Navegador ──► Spring Security (¿sesión? ¿rol?) ──► @Controller ──► JpaRepository ──► H2 / MySQL
                                                       │
                                                       └──► vista Thymeleaf (templates/)
```

```
com.Vetecare.app
├── VetecareApplication.java
├── model/        Entidades JPA y enumeraciones (Usuario, Rol, Propietario, Mascota, Especie, Sexo)
├── repository/   Interfaces JpaRepository con métodos derivados y @Query
├── controller/   Rutas web, validación de flujo y elección de la vista
└── config/       SecurityConfig (rutas por rol, BCrypt) y DatosIniciales (datos de demostración)
resources/
├── templates/    fragments.html (head, menú, alertas), login, inicio, acceso-denegado,
│                 usuarios/, propietarios/, mascotas/  (en S2 y S3: citas/, consultas/, vacunas/, inventario/, reportes/)
├── static/css/   vetcare.css (paleta azul oscuro, azul claro y dorado)
└── application.properties
```

Ver [ADR-001](../docs/adr/ADR-001-sin-capa-service.md).

## 3. Modelo de datos

Base `VetCareDB`, motor InnoDB, `utf8mb4`. Claves primarias autoincrementales.

| Tabla | Campos | Sprint |
|---|---|---|
| `usuarios` | id, nombre, correo (UNIQUE), password (BCrypt), rol, activo, propietario_id (FK, opcional) | 1 |
| `propietarios` | id, nombre, documento (UNIQUE), telefono, correo, direccion | 1 |
| `mascotas` | id, nombre, especie, raza, sexo, fecha_nacimiento, peso, propietario_id (FK) | 1 |
| `citas` | id, fecha_hora, motivo, estado, mascota_id, veterinario_id, recepcionista_id | 2 |
| `consultas` | id, cita_id (UNIQUE), diagnostico, tratamiento, peso, observaciones | 2 |
| `vacunas` | id, mascota_id, nombre, fecha_aplicacion, intervalo_dias, fecha_refuerzo, veterinario_id | 2 |
| `productos` | id, nombre, categoria, precio, stock, stock_minimo, fecha_vencimiento | 2 |
| `movimientos_inventario` | id, producto_id, usuario_id, tipo, cantidad, motivo, fecha_movimiento | 2 |
| `detalle_consulta` | id, consulta_id, producto_id, cantidad, observaciones | 2 |

Relaciones: Propietarios 1:N Mascotas · Mascotas 1:N Citas y Vacunas · Usuarios 1:N Citas (veterinario y recepcionista)
· Citas 1:0..1 Consultas · Consultas 1:N DetalleConsulta · Productos 1:N Movimientos.

## 4. Rutas y permisos

| Ruta | ADMIN | RECEPCIONISTA | VETERINARIO | AUXILIAR | PROPIETARIO |
|---|:-:|:-:|:-:|:-:|:-:|
| `/login`, `/css/**` | público | público | público | público | público |
| `/` (inicio) | ✓ | ✓ | ✓ | ✓ | ✓ |
| `/usuarios/**` | ✓ | | | | |
| `/propietarios/**`, `/mascotas/**` | ✓ | ✓ | | | |
| `/h2-console/**` (solo desarrollo) | ✓ | | | | |
| `/citas/**` (S2) | ✓ | ✓ | | | |
| `/consultas/**`, `/vacunas/**`, `/agenda` (S2) | | | ✓ | | |
| `/inventario/**` (S2) | | | | ✓ | |
| `/tablero`, `/reportes/**` (S3) | ✓ | | | | |
| `/mi-cuenta/**` (S3) | | | | | ✓ |

Ver [ADR-003](../docs/adr/ADR-003-login-por-correo-y-roles.md).

## 5. Configuración

| Variable | Uso | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC | `jdbc:h2:file:./data/vetcaredb;MODE=MySQL;DATABASE_TO_LOWER=TRUE` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales | `sa` / vacío |
| `DDL_AUTO` | Estrategia de Hibernate | `update` (usar `validate` cuando el esquema MySQL esté estable) |
| `PORT` | Puerto HTTP (Render) | `8080` |
| `H2_CONSOLE` | Consola H2 | `true` (poner `false` en producción) |

Ver [ADR-002](../docs/adr/ADR-002-h2-local-mysql-nube.md).

## 6. Decisiones de arquitectura (ADR)

| ADR | Decisión |
|---|---|
| [ADR-001](../docs/adr/ADR-001-sin-capa-service.md) | Controladores directo a repositorios, sin capa service |
| [ADR-002](../docs/adr/ADR-002-h2-local-mysql-nube.md) | H2 en desarrollo; MySQL por variables de entorno |
| [ADR-003](../docs/adr/ADR-003-login-por-correo-y-roles.md) | Login por correo, BCrypt y un rol por cuenta |
