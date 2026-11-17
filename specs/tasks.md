# Tareas — Sprint 1: especificación, acceso por rol, propietarios y mascotas

**Sprint Goal:** especificación v1.0 lista para la Puerta 1, los cinco roles inician sesión y ven solo su menú, y la recepcionista
registra propietarios y mascotas que quedan guardados en la base de datos.

**Puerta 3:** revisión de los Developers · **Puntos:** 13

## Tareas

| # | Tarea | Requisitos | Criterios | Hace | Valida | Estado |
|---|---|---|---|---|---|---|
| T-01 | Constitución del proyecto | — | — | Juan Manuel | Juan David | Hecho |
| T-02 | `spec.md` v1.0 (12 HU en EARS y Given/When/Then) | todos | todos | Juan Manuel | Juan David | Hecho, falta Puerta 1 |
| T-03 | `plan.md` y ADR-001 a ADR-003 | — | — | Juan David | Juan Manuel | Hecho |
| T-04 | Dependencias: Security, extras de Thymeleaf, conector MySQL | — | — | Juan David | Juan Manuel | Hecho |
| T-05 | `application.properties` con variables de entorno (H2 / MySQL) | RNF-04 | — | Juan David | Juan Manuel | Hecho |
| T-06 | Entidades `Usuario`, `Rol`; `UsuarioRepository` | REQ-VET-003, 006, 007 | AC-006 | Juan David | Juan Manuel | Hecho |
| T-07 | `SecurityConfig`: login por correo, BCrypt, rutas por rol, página 403 | REQ-VET-001, 002, 004, 008 | AC-001 a AC-004, AC-011 | Juan David | Juan Manuel | Hecho |
| T-08 | Plantilla base: menú lateral por rol, barra móvil, alertas | REQ-VET-005 | AC-005 | Juan Manuel | Juan David | Hecho |
| T-09 | Login, inicio por rol y acceso denegado | REQ-VET-001, 004, 005 | AC-001, AC-004 | Juan Manuel | Juan David | Hecho |
| T-10 | CRUD de usuarios (crear, editar, activar/desactivar) | REQ-VET-006, 007, 008 | AC-010, AC-011 | Juan David | Juan Manuel | Hecho |
| T-11 | Entidad `Propietario`, repositorio y CRUD con búsqueda | REQ-VET-009 a 012 | AC-007, AC-012 | Juan David | Juan Manuel | Hecho |
| T-12 | Entidad `Mascota`, repositorio y CRUD con filtro por propietario | REQ-VET-013 a 015 | AC-008, AC-009, AC-013 | Juan Manuel | Juan David | Hecho |
| T-13 | Datos de demostración: 5 usuarios, 5 propietarios, 10 mascotas | — | — | Juan David | Juan Manuel | Hecho |
| T-14 | Pruebas automáticas de los criterios | — | AC-001 a AC-009 | Juan Manuel | Juan David | Hecho |
| T-15 | Conectar MySQL en la nube y probar CRUD | RNF-05 | — | Juan David | Juan Manuel | Pendiente |
| T-16 | Demo en la Sprint Review y retrospectiva | — | — | Equipo | PO | Pendiente |

## Matriz de trazabilidad (Sprint 1)

| Requisito | Criterio | Tarea | Evidencia |
|---|---|---|---|
| REQ-VET-001 | AC-001 | T-07, T-09 | `loginConCredencialesValidasEntraConSuRol` |
| REQ-VET-002 | AC-002 | T-07 | `loginConContrasenaIncorrectaEsRechazado` |
| REQ-VET-003 | AC-006 | T-06, T-13 | `lasContrasenasSeGuardanCifradasConBCrypt` |
| REQ-VET-004 | AC-003 | T-07 | `sinSesionSeRedirigeAlLogin` |
| REQ-VET-004 | AC-004 | T-07, T-09 | `recepcionistaNoPuedeEntrarAUsuarios` |
| REQ-VET-005 | AC-005 | T-08 | `recepcionistaVeSoloSuMenu` |
| REQ-VET-006 | AC-010 | T-10 | Prueba manual: crear un usuario con correo repetido |
| REQ-VET-008 | AC-011 | T-07, T-10 | Prueba manual: desactivar al auxiliar e intentar entrar |
| REQ-VET-010 | AC-007 | T-11 | `propietarioConDocumentoRepetidoNoSeGuarda` |
| REQ-VET-011 | AC-012 | T-11 | Prueba manual: eliminar a Carlos Andrés Ruiz |
| REQ-VET-013 | AC-008 | T-12 | `mascotaSinPropietarioNoSeGuarda` |
| REQ-VET-013 | AC-009 | T-12 | `mascotaConPropietarioExistenteSeGuarda` |
| REQ-VET-014 | AC-013 | T-12 | Prueba manual: fecha de nacimiento de mañana |

Las pruebas automáticas están en `src/test/java/com/Vetecare/app/VetecareApplicationTests.java` y se ejecutan con `mvnw test`.
