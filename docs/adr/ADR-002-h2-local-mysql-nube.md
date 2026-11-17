# ADR-002: H2 en desarrollo y MySQL por variables de entorno

**Estado:** aceptada · **Fecha:** 2026-11-17

## Contexto
La entrega final usa MySQL en la nube, pero no todos los equipos de desarrollo tienen MySQL instalado y las credenciales
no pueden ir en el repositorio.

## Decisión
- `spring.datasource.url=${DB_URL:jdbc:h2:file:./data/vetcaredb;MODE=MySQL;...}`: sin variables, la aplicación usa H2 guardado en `./data` (ignorado por Git).
- Con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, la misma aplicación se conecta a MySQL sin cambiar código. Spring detecta el driver por la URL.
- H2 corre en modo compatible con MySQL para detectar diferencias temprano.
- Las pruebas usan H2 en memoria (`jdbc:h2:mem:vetcaretest`) para no tocar los datos locales.

## Consecuencias
- Cualquier integrante clona el repositorio y ejecuta la aplicación sin instalar nada más.
- Antes de cada Sprint Review se prueba al menos una vez contra MySQL en la nube.
