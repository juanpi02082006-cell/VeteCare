# ADR-001: Controladores directo a repositorios, sin capa service

**Estado:** aceptada · **Fecha:** 2026-11-17

## Contexto
El curso exige que el proyecto no tenga paquete ni capa Service.

## Decisión
- Los `@Controller` reciben los repositorios por constructor y coordinan la operación: validan, consultan, guardan y eligen la vista.
- La lógica de dominio pequeña va en la entidad (por ejemplo, `Mascota.getEdad()`; en el Sprint 2, `Producto.estaCritico()` y `Vacuna.calcularFechaRefuerzo()`).
- Las consultas complejas van en el repositorio con métodos derivados o `@Query`.
- Lo transversal (usuario actual en el menú, recorte de textos vacíos) va en un `@ControllerAdvice`.

## Consecuencias
- Menos clases y un flujo fácil de seguir: ruta → controlador → repositorio → vista.
- Las reglas que tocan varias entidades (solapamiento de citas, movimientos de inventario) quedan en el controlador; si una operación modifica varias tablas, se marca el método con `@Transactional`.
