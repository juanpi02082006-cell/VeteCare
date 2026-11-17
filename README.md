# VetCare Santander

Plataforma web para una clínica veterinaria con componente de Sistema de Soporte a Decisiones.
Proyecto de Programación en Java (B193), Universitaria Tecnológica de Santander.

## Ejecutar

Desde Spring Tools: clic derecho en el proyecto → **Run As → Spring Boot App**.
Desde la terminal: `mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

Abrir <http://localhost:8080>.

## Cuentas de demostración

Todas usan la contraseña **`Vetcare2026`** (datos ficticios).

| Rol | Correo |
|---|---|
| Administrador | `admin@vetcare.com` |
| Veterinario | `veterinario@vetcare.com` |
| Recepcionista | `recepcion@vetcare.com` |
| Auxiliar de inventario | `inventario@vetcare.com` |
| Propietario | `carlos.ruiz@correo.com` |

## Base de datos

Sin configuración, usa H2 guardado en `./data` (la consola está en `/h2-console`, solo para el administrador;
URL `jdbc:h2:file:./data/vetcaredb`, usuario `sa`, sin contraseña).

Para MySQL, definir las variables de entorno:

```
DB_URL=jdbc:mysql://HOST:3306/VetCareDB?useSSL=true&serverTimezone=America/Bogota
DB_USERNAME=...
DB_PASSWORD=...
```

## Pruebas

`mvnw test`: 10 pruebas de los criterios de aceptación del Sprint 1.

## Documentos SDD

- [Constitución](specs/constitution.md)
- [Especificación v1.0](specs/spec.md)
- [Plan técnico](specs/plan.md)
- [Tareas del Sprint 1 y trazabilidad](specs/tasks.md)
- [Decisiones de arquitectura](docs/adr/)
