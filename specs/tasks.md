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

Las pruebas automáticas del Sprint 1 están en `src/test/java/com/Vetecare/app/VetecareApplicationTests.java`.

---

# Tareas — Sprint 2: operación clínica, vacunación e inventario

**Sprint Goal:** agendar citas sin cruces, registrar consultas y vacunas con cálculo de refuerzo, y controlar entradas y salidas
del inventario con alertas tempranas.

**Puntos:** 13

## Tareas

| # | Tarea | Requisitos | Criterios | Hace | Valida | Estado |
|---|---|---|---|---|---|---|
| T-17 | Entidades `Cita`, `EstadoCita` y `CitaRepository` (consulta de cruces) | REQ-VET-016 a 019 | AC-014 a AC-016 | Juan David | Juan Manuel | Hecho |
| T-18 | `CitaController` y vistas: agenda por día con filtros, agendar, modificar y cancelar | REQ-VET-016 a 019 | AC-014 a AC-016 | Juan David | Juan Manuel | Hecho |
| T-19 | Agenda del veterinario | REQ-VET-020 | — | Juan Manuel | Juan David | Hecho |
| T-20 | Entidades `Consulta` (1:1 con cita) y `DetalleConsulta` | REQ-VET-020 a 022 | AC-017, AC-018 | Juan Manuel | Juan David | Hecho |
| T-21 | `ConsultaController`: registrar, ver detalle, productos usados e historial de la mascota | REQ-VET-020 a 022 | AC-017, AC-018 | Juan Manuel | Juan David | Hecho |
| T-22 | Entidad `Vacuna` con `calcularFechaRefuerzo()` y estado del refuerzo | REQ-VET-023, 024 | AC-019 | Juan David | Juan Manuel | Hecho |
| T-23 | `VacunaController` y vistas (vista previa del refuerzo y filtro de pendientes) | REQ-VET-023, 024 | AC-019 | Juan David | Juan Manuel | Hecho |
| T-24 | Entidades `Producto` y `MovimientoInventario`; CRUD de productos con stock inicial | REQ-VET-025 | AC-021 | Juan Manuel | Juan David | Hecho |
| T-25 | `MovimientoController`: entradas y salidas sin stock negativo | REQ-VET-025, 026 | AC-020, AC-021 | Juan Manuel | Juan David | Hecho |
| T-26 | Alertas de stock crítico, vencidos y por vencer, con contador en el menú | REQ-VET-027, 028 | AC-022, AC-023 | Juan David | Juan Manuel | Hecho |
| T-27 | Rutas por rol en `SecurityConfig`, menú e inicio con los números de cada rol | REQ-VET-004, 005 | — | Juan David | Juan Manuel | Hecho |
| T-28 | Datos de demostración del Sprint 2 | — | — | Juan David | Juan Manuel | Hecho |
| T-29 | Pruebas automáticas de los criterios | — | AC-014 a AC-023 | Juan Manuel | Juan David | Hecho |
| T-30 | Demo en la Sprint Review y retrospectiva | — | — | Equipo | PO | Pendiente |

## Matriz de trazabilidad (Sprint 2)

Evidencias en `src/test/java/com/Vetecare/app/Sprint2Tests.java`.

| Requisito | Criterio | Tarea | Evidencia |
|---|---|---|---|
| REQ-VET-018 | AC-014 | T-17, T-18 | `citaQueSeCruzaConOtraDelMismoVeterinarioSeRechaza` |
| REQ-VET-017 | AC-015 | T-18 | `citaEnFechaPasadaSeRechaza` |
| REQ-VET-019 | AC-016 | T-18 | `cancelarUnaCitaLiberaElHorario` |
| REQ-VET-021 | AC-017 | T-21 | `segundaConsultaParaLaMismaCitaSeRechaza` |
| REQ-VET-020 | AC-018 | T-21 | `consultaParaUnaCitaCanceladaSeRechaza` |
| REQ-VET-020 | — | T-21 | `consultaValidaMarcaLaCitaComoAtendidaYActualizaElPeso` |
| REQ-VET-022 | — | T-21 | `productoVencidoNoSePuedeUsarEnUnaConsulta`, `productoUsadoEnConsultaDescuentaStockYRegistraSalida` |
| REQ-VET-023 | AC-019 | T-22, T-23 | `laFechaDeRefuerzoSumaElIntervaloALaAplicacion`, `alRegistrarUnaVacunaSeGuardaSuFechaDeRefuerzo` |
| REQ-VET-026 | AC-020 | T-25 | `salidaMayorAlStockSeRechazaYElStockNoCambia` |
| REQ-VET-025 | AC-021 | T-25 | `entradaSumaAlStockYQuedaRegistrada` |
| REQ-VET-027 | AC-022 | T-26 | `productoConStockIgualAlMinimoEsCritico`, `laPantallaDeAlertasMuestraLosProductosDeLosDatosDePrueba` |
| REQ-VET-028 | AC-023 | T-26 | `productoQueVenceDentroDe30DiasEstaPorVencer`, `laPantallaDeAlertasMuestraLosProductosDeLosDatosDePrueba` |
| REQ-VET-004 | — | T-27 | `recepcionistaNoEntraAlInventario`, `auxiliarNoEntraACitasNiConsultas` |

## Decisiones tomadas durante el sprint

- Las consultas y las vacunas no se editan ni se eliminan, para conservar la trazabilidad clínica. Un error se corrige con una nueva entrada.
- El stock de un producto solo cambia con movimientos. Al crear el producto, el stock inicial queda registrado como una entrada.
- Usar un producto en una consulta crea su salida de inventario automáticamente (`Consulta #id`).
- Una vacuna ya no cuenta como pendiente cuando hay una aplicación más reciente de la misma vacuna para la misma mascota ("Reforzada").
- El peso registrado en la consulta actualiza el peso de la mascota.
