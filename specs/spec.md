# Especificación — VetCare Santander

**Versión:** 1.0 · **Fecha:** 2026-11-17 · **Estado:** pendiente de aprobación del PO (Puerta 1)

## 1. Contexto

La clínica guarda clientes, mascotas, citas, historias clínicas e inventario en documentos separados. Eso produce datos
duplicados, citas cruzadas, demoras para consultar antecedentes, refuerzos de vacunas olvidados y productos vencidos o agotados.

**Product Goal:** que ningún proceso clínico o administrativo pierda su trazabilidad. Cada cita, consulta, vacuna y movimiento de
inventario queda registrado, validado y bajo el control del rol autorizado, y alimenta indicadores y alertas (SSD).

## 2. Actores

| Rol (código) | Puede |
|---|---|
| Administrador (`ADMIN`) | Gestionar usuarios y roles, ver el tablero SSD, exportar reportes, supervisar la operación. |
| Veterinario (`VETERINARIO`) | Ver su agenda, registrar consultas y vacunas, consultar razas. |
| Recepcionista (`RECEPCIONISTA`) | Gestionar propietarios y mascotas; agendar, modificar y cancelar citas. |
| Auxiliar de inventario (`AUXILIAR`) | Gestionar productos y movimientos; recibir alertas de stock y vencimiento. |
| Propietario (`PROPIETARIO`) | Ver solo sus mascotas, sus citas y el carnet de vacunación con QR. |

## 3. Reglas de negocio

- **RN-01** Un propietario puede tener varias mascotas; cada mascota pertenece a un solo propietario.
- **RN-02** Un veterinario no puede tener dos citas activas que se solapen.
- **RN-03** No se agendan citas en fechas u horas pasadas.
- **RN-04** Una consulta se asocia a una cita válida y no puede haber dos consultas para la misma cita.
- **RN-05** Una mascota puede tener varios registros de vacunación.
- **RN-06** Fecha de refuerzo = fecha de aplicación + intervalo en días de la vacuna.
- **RN-07** Una salida de inventario no puede superar el stock disponible.
- **RN-08** No se usan productos vencidos en una consulta.
- **RN-09** El propietario solo accede a sus propias mascotas y datos relacionados.
- **RN-10** Las cuentas no se borran: se desactivan. El administrador no puede desactivarse a sí mismo.

## 4. Requisitos y criterios de aceptación

Sintaxis EARS: *El sistema deberá…* (ubicuo) · *Cuando…* (evento) · *Mientras…* (estado) · *Si…, entonces…* (no deseado).

### Sprint 1

#### HU-01 · Administrador: crear usuarios y asignar roles

- **REQ-VET-001** Cuando un usuario envíe el correo y la contraseña de una cuenta activa, el sistema deberá iniciar la sesión y mostrar el inicio con las opciones de su rol.
- **REQ-VET-002** Si las credenciales son incorrectas o la cuenta está inactiva, entonces el sistema deberá rechazar el ingreso con un mensaje que no revele cuál dato falló.
- **REQ-VET-003** El sistema deberá guardar las contraseñas cifradas con BCrypt.
- **REQ-VET-004** Si un usuario sin sesión pide una ruta protegida, entonces el sistema deberá llevarlo al inicio de sesión; si un usuario con sesión pide una ruta que su rol no permite, entonces el sistema deberá negar el acceso (HTTP 403).
- **REQ-VET-005** Mientras haya una sesión iniciada, el sistema deberá mostrar en el menú solo las opciones del rol de esa cuenta.
- **REQ-VET-006** Cuando el administrador cree o edite un usuario, el sistema deberá exigir nombre, correo único, uno de los cinco roles y una contraseña de al menos 8 caracteres (obligatoria al crear; si se deja vacía al editar, se conserva la anterior).
- **REQ-VET-007** Cuando el rol asignado sea Propietario, el sistema deberá exigir que la cuenta quede vinculada a un propietario registrado.
- **REQ-VET-008** Cuando el administrador desactive una cuenta, el sistema deberá impedir que esa cuenta inicie sesión (RN-10).

| AC | Given | When | Then |
|---|---|---|---|
| AC-001 | existe la cuenta activa `admin@vetcare.com` | inicia sesión con su contraseña correcta | queda autenticado con rol ADMIN y llega al inicio |
| AC-002 | existe la cuenta `admin@vetcare.com` | inicia sesión con una contraseña incorrecta | no se autentica y vuelve al login con un mensaje de error |
| AC-003 | no hay sesión iniciada | se pide `/propietarios` | se redirige a `/login` |
| AC-004 | hay sesión de recepcionista | se pide `/usuarios` | responde 403 y muestra "No tienes acceso" |
| AC-005 | hay sesión de recepcionista | abre cualquier pantalla | el menú muestra Propietarios y Mascotas, y no muestra Usuarios |
| AC-006 | se creó una cuenta con contraseña `Vetcare2026` | se lee la columna `password` | contiene un hash BCrypt (`$2…`) y no el texto plano |
| AC-010 | existe una cuenta con el correo `x@vetcare.com` | el administrador crea otra con el mismo correo | no se guarda y el campo correo muestra "Ese correo ya está registrado" |
| AC-011 | el administrador desactivó la cuenta del auxiliar | el auxiliar intenta iniciar sesión | el ingreso es rechazado |

#### HU-02 · Recepcionista: registrar y actualizar propietarios

- **REQ-VET-009** Cuando la recepcionista registre o actualice un propietario, el sistema deberá exigir nombre, documento (5 a 15 dígitos) y teléfono (7 a 10 dígitos), y validar el formato del correo si se ingresa.
- **REQ-VET-010** Si el documento ya pertenece a otro propietario, entonces el sistema deberá rechazar el registro y señalar el campo.
- **REQ-VET-011** Si se intenta eliminar un propietario que tiene mascotas o una cuenta de usuario, entonces el sistema deberá impedirlo y explicar el motivo.
- **REQ-VET-012** El sistema deberá permitir buscar propietarios por nombre o documento.

| AC | Given | When | Then |
|---|---|---|---|
| AC-007 | existe un propietario con documento `1098765432` | se registra otro con ese documento | no se guarda y el campo documento muestra el error |
| AC-012 | el propietario tiene 2 mascotas | la recepcionista lo elimina | no se elimina y aparece "tiene mascotas registradas" |

#### HU-03 · Recepcionista: registrar mascotas de un propietario existente

- **REQ-VET-013** Cuando la recepcionista registre una mascota, el sistema deberá exigir nombre, especie, sexo y un propietario existente (RN-01).
- **REQ-VET-014** Si la fecha de nacimiento es futura o el peso no está entre 0 y 150 kg, entonces el sistema deberá rechazar el registro y señalar el campo.
- **REQ-VET-015** El sistema deberá listar las mascotas con su propietario y permitir filtrarlas por propietario o buscarlas por nombre de mascota o de propietario.

| AC | Given | When | Then |
|---|---|---|---|
| AC-008 | hay sesión de recepcionista | guarda una mascota sin propietario | no se guarda y el campo propietario muestra el error |
| AC-009 | existe el propietario Carlos Andrés Ruiz | guarda una mascota con ese propietario | se guarda y aparece en el listado |
| AC-013 | hay sesión de recepcionista | guarda una mascota con fecha de nacimiento de mañana | no se guarda y el campo fecha muestra "No puede ser una fecha futura" |

### Sprint 2

#### HU-04 · Recepcionista: agendar, modificar y cancelar citas

- **REQ-VET-016** Cuando la recepcionista agende o modifique una cita, el sistema deberá exigir fecha y hora, motivo, mascota y veterinario activo.
- **REQ-VET-017** Si la fecha y hora es anterior al momento actual, entonces el sistema deberá rechazar la cita (RN-03).
- **REQ-VET-018** Si el veterinario ya tiene una cita activa que se solapa con el intervalo pedido, entonces el sistema deberá rechazar la cita con un mensaje claro y sin modificar los datos existentes (RN-02).
- **REQ-VET-019** Cuando se cancele una cita, el sistema deberá cambiar su estado a CANCELADA, conservar el registro y liberar ese horario.

| AC | Given | When | Then |
|---|---|---|---|
| AC-014 | la Dra. Ortiz tiene cita activa el 2026-11-25 a las 10:00 | se agenda otra para ella a las 10:15 | se rechaza con "El veterinario ya tiene una cita en ese horario" |
| AC-015 | hoy es 2026-11-24 | se agenda una cita para el 2026-11-23 | se rechaza con "No se permiten fechas pasadas" |
| AC-016 | la cita de las 10:00 fue cancelada | se agenda otra a las 10:00 con la misma veterinaria | se guarda |

#### HU-05 · Veterinario: registrar consultas

- **REQ-VET-020** Cuando el veterinario registre una consulta, el sistema deberá exigir una cita válida (no cancelada), diagnóstico y tratamiento, y permitir registrar peso y observaciones.
- **REQ-VET-021** Si la cita ya tiene una consulta, entonces el sistema deberá rechazar la segunda (RN-04).
- **REQ-VET-022** Si un producto usado en la consulta está vencido, entonces el sistema deberá rechazarlo (RN-08).

| AC | Given | When | Then |
|---|---|---|---|
| AC-017 | la cita #5 ya tiene consulta | se registra otra consulta para la cita #5 | se rechaza |
| AC-018 | la cita #6 está cancelada | se registra una consulta para ella | se rechaza |

#### HU-06 · Veterinario: registrar vacunas con cálculo de refuerzo

- **REQ-VET-023** Cuando el veterinario registre una vacuna, el sistema deberá calcular la fecha de refuerzo sumando el intervalo en días a la fecha de aplicación (RN-06).
- **REQ-VET-024** El sistema deberá mostrar el historial de vacunas de cada mascota con su fecha de refuerzo.

| AC | Given | When | Then |
|---|---|---|---|
| AC-019 | aplicación 2026-11-20 e intervalo 365 días | se guarda la vacuna | la fecha de refuerzo es 2027-11-20 |

#### HU-07 · Auxiliar: productos y movimientos de inventario

- **REQ-VET-025** Cuando el auxiliar registre una entrada, el sistema deberá sumar la cantidad al stock y guardar el movimiento con usuario, fecha y motivo.
- **REQ-VET-026** Si una salida supera el stock disponible, entonces el sistema deberá rechazarla sin cambiar el stock (RN-07).

| AC | Given | When | Then |
|---|---|---|---|
| AC-020 | un producto tiene stock 5 | se registra una salida de 8 | se rechaza y el stock sigue en 5 |
| AC-021 | un producto tiene stock 5 | se registra una entrada de 10 | el stock queda en 15 y hay un movimiento ENTRADA |

#### HU-08 · Auxiliar: alertas de stock crítico y vencimiento

- **REQ-VET-027** El sistema deberá marcar como crítico un producto cuando su stock sea menor o igual que su stock mínimo.
- **REQ-VET-028** El sistema deberá marcar como próximo a vencer un producto cuya fecha de vencimiento esté dentro de los próximos 30 días.

| AC | Given | When | Then |
|---|---|---|---|
| AC-022 | stock 3 y stock mínimo 3 | se abren las alertas | el producto aparece como crítico |
| AC-023 | hoy es 2026-11-24 y vence el 2026-12-10 | se abren las alertas | aparece como próximo a vencer |

### Sprint 3

#### HU-09 · Administrador: tablero SSD

- **REQ-VET-029** Mientras el administrador esté en el tablero, el sistema deberá mostrar tarjetas con citas del mes, productos críticos, próximos vencimientos y vacunas pendientes, y gráficas de citas por mes y ocupación por veterinario (Chart.js).

| AC | Given | When | Then |
|---|---|---|---|
| AC-024 | hay 2 productos críticos | el administrador abre el tablero | la tarjeta de productos críticos muestra 2 |

#### HU-10 · Administrador: exportar reportes

- **REQ-VET-030** Cuando el administrador pida un reporte de inventario, citas o alertas, el sistema deberá generarlo en Excel (.xlsx, Apache POI) o PDF (OpenPDF) con los datos actuales.

| AC | Given | When | Then |
|---|---|---|---|
| AC-025 | hay 10 productos | se exporta el inventario a Excel | el archivo tiene 10 filas de datos con nombre, stock, stock mínimo y vencimiento |

#### HU-11 · Propietario: mis mascotas, mis citas y carnet con QR

- **REQ-VET-031** Mientras el propietario tenga sesión, el sistema deberá mostrarle solo las mascotas y citas vinculadas a su cuenta (RN-09).
- **REQ-VET-032** Si el propietario pide una mascota de otra cuenta, entonces el sistema deberá negar el acceso.
- **REQ-VET-033** Cuando el propietario descargue el carnet, el sistema deberá generar un PDF con las vacunas y un QR (ZXing) que lleve a una ficha pública limitada, sin datos sensibles.

| AC | Given | When | Then |
|---|---|---|---|
| AC-026 | Carlos tiene a Max y Luna | abre "Mis mascotas" | ve solo a Max y Luna |
| AC-027 | Rocky pertenece a Laura | Carlos pide la ficha de Rocky | el acceso es negado |

#### HU-12 · Veterinario: información de razas desde una API externa

- **REQ-VET-034** Cuando el veterinario abra la ficha de un perro o un gato, el sistema deberá consultar TheDogAPI o TheCatAPI y mostrar la información y la imagen de la raza.
- **REQ-VET-035** Si la API no responde en 5 segundos o devuelve un error, entonces el sistema deberá mostrar la ficha sin esa sección y un aviso breve, sin afectar el resto del sistema.

| AC | Given | When | Then |
|---|---|---|---|
| AC-028 | la API no está disponible | el veterinario abre la ficha de Max | la ficha carga y muestra "Información de raza no disponible" |

## 5. Requisitos no funcionales

- **RNF-01** Seguridad: BCrypt y rutas protegidas por rol.
- **RNF-02** Usabilidad: interfaz responsive con Bootstrap 5, mensajes en español junto al campo.
- **RNF-03** Rendimiento: listados habituales en menos de 3 s.
- **RNF-04** Configuración: secretos en variables de entorno.
- **RNF-05** Disponibilidad: aplicación en Render y MySQL en la nube.

## 6. Dudas aclaradas (supuestos para confirmar con el PO)

| # | Pregunta | Respuesta asumida |
|---|---|---|
| 1 | ¿Con qué se inicia sesión? | Con el correo de la cuenta. |
| 2 | ¿Se pueden borrar usuarios? | No; se desactivan (RN-10). |
| 3 | ¿Se puede eliminar un propietario con mascotas? | No; primero se eliminan o reasignan sus mascotas. |
| 4 | ¿Cuánto dura una cita para calcular solapamientos? | 30 minutos. |
| 5 | ¿Qué es "próximo a vencer"? | Vence dentro de los próximos 30 días. |
| 6 | ¿Qué es una vacuna pendiente? | Refuerzo vencido o dentro de los próximos 15 días. |
| 7 | ¿El administrador también gestiona propietarios y mascotas? | Sí, además de la recepcionista. |
