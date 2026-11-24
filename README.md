# VetCare Santander

Plataforma web para una clínica veterinaria con componente de Sistema de Soporte a Decisiones.
Proyecto de Programación en Java (B193), Universitaria Tecnológica de Santander.

## Ejecutar

Desde Spring Tools: clic derecho en el proyecto → **Run As → Spring Boot App**.
Desde la terminal: `mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

Abrir <http://localhost:8080>.

## En línea

Publicada en Clever Cloud: <https://vetcare-santander.cleverapps.io>
(app Java + Maven en Montreal, base MySQL DEV vinculada, perfil `clevercloud`).

Variables configuradas en Clever Cloud: `CC_JAVA_VERSION=21`, `CC_MAVEN_BUILD_GOAL=clean package -DskipTests`,
`CC_JAR_PATH=target/Vetecare-0.0.1-SNAPSHOT.jar`, `SPRING_PROFILES_ACTIVE=clevercloud` y `TZ=America/Bogota`.
Las credenciales de MySQL las inyecta Clever Cloud (`MYSQL_ADDON_*`). Para publicar cambios: `clever deploy`.

## Cuentas de demostración

Todas usan la contraseña **`12345`** (datos ficticios).

| Rol | Correo |
|---|---|
| Administrador | `admin@vetcare.com` |
| Veterinario | `veterinario@vetcare.com` (Dra. Valentina Ortiz) |
| Veterinario | `veterinario2@vetcare.com` (Dr. Mateo Rincón) |
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

`mvnw test`: 32 pruebas de los criterios de aceptación de los sprints 1 y 2.

## Datos de demostración

Se cargan solos al arrancar, cada sprint por separado y solo si sus tablas están vacías:

- **Sprint 1:** 5 usuarios, 5 propietarios y 10 mascotas.
- **Sprint 2:** un segundo veterinario, 12 citas (8 atendidas con consulta, 1 cancelada y 3 programadas, dos de ellas para hoy),
  10 vacunas con refuerzos vencidos, próximos y al día, y 10 productos (2 con stock crítico, 1 vencido y 1 por vencer).

Para empezar de cero, detener la aplicación y borrar la carpeta `data/`.

## Documentos SDD

- [Constitución](specs/constitution.md)
- [Especificación v1.0](specs/spec.md)
- [Plan técnico](specs/plan.md)
- [Tareas y trazabilidad de los sprints 1 y 2](specs/tasks.md)
- [Decisiones de arquitectura](docs/adr/)
