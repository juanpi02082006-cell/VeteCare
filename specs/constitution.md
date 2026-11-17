# Constitución del proyecto — VetCare Santander

**Versión:** 1.0 · **Fecha:** 2026-11-17 · **Puerta 0:** aprobada por el equipo

Principios no negociables. Toda especificación, plan, tarea y línea de código debe respetarlos.
Si una tarea entra en conflicto con esta constitución, se detiene y se discute en el Daily Scrum.

## 1. La especificación manda

1. `specs/spec.md` es la referencia principal. No se programa nada que no esté en un requisito (`REQ-VET-###`).
2. Cada requisito se escribe en EARS y tiene al menos un criterio de aceptación (`AC-###`) en formato Given/When/Then.
3. Un cambio pedido después de la Puerta 1 se describe primero en `specs/delta-spec.md` y lo aprueba el PO antes de tocar el código.

## 2. Arquitectura

1. **Sin capa ni carpeta Service** (condición del curso). Los `@Controller` trabajan directamente con los repositorios JPA y las entidades.
2. Paquetes: `model`, `repository`, `controller`, `config`. Vistas en `templates/`, recursos en `static/`.
3. La lógica de dominio sencilla vive en la entidad (por ejemplo `estaCritico()`, `calcularFechaRefuerzo()`); las consultas en el repositorio.
4. Persistencia solo con Spring Data JPA. Prohibido usar listas en memoria como sustituto de la base de datos.

## 3. Seguridad y datos personales

1. Contraseñas siempre con BCrypt. Nunca se muestran ni se envían al navegador, ni siquiera como hash.
2. Cada ruta se protege por rol en `SecurityConfig`. La interfaz muestra solo las opciones del rol, pero la protección real está en el servidor.
3. Credenciales y secretos en variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`), nunca en el repositorio.
4. Datos de prueba siempre ficticios (Ley 1581 de 2012). El QR del carnet no expone información clínica ni personal sensible.

## 4. Calidad

1. **Cuatro ojos:** quien implementa una historia no es quien la valida (ver Tabla 5 del plan de trabajo).
2. Cada criterio de aceptación verificable automáticamente tiene su prueba, marcada con el id del criterio (`// AC-###`).
3. Ningún cambio llega a `main` si `mvnw test` falla.
4. Mensajes de error claros y en español, junto al campo que los causa.

## 5. Uso de inteligencia artificial

1. La IA genera borradores de código y documentos a partir de la especificación; el equipo los revisa, los entiende y responde por ellos.
2. Todo uso relevante de IA queda en la bitácora (`docs/bitacora-ia.md`): fecha, herramienta, qué se pidió y qué se ajustó a mano.
3. La IA no inventa requisitos: si detecta un vacío, lo registra como pregunta para el PO.

## 6. Definición de Hecho

Una historia está terminada cuando cumple sus criterios de aceptación, valida los datos obligatorios, respeta permisos y reglas
de negocio, persiste en la base de datos, muestra mensajes claros, fue probada con casos válidos e inválidos, no rompe lo existente
y fue revisada por el otro integrante antes de integrarse.
