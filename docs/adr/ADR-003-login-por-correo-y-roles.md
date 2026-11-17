# ADR-003: Login por correo, BCrypt y un rol por cuenta

**Estado:** aceptada · **Fecha:** 2026-11-17

## Contexto
El sistema tiene cinco roles y cada uno debe ver y usar solo sus módulos.

## Decisión
- El usuario inicia sesión con su correo (`usernameParameter("correo")`). El `UserDetailsService` es un bean en `SecurityConfig` que busca en `UsuarioRepository`.
- Contraseñas con `BCryptPasswordEncoder`. El formulario de usuarios nunca rellena el campo contraseña.
- Cada cuenta tiene un solo `Rol`: ADMIN, VETERINARIO, RECEPCIONISTA, AUXILIAR o PROPIETARIO.
- La protección real se hace por ruta en `SecurityConfig`. El menú usa `sec:authorize` solo para ocultar lo que el rol no puede usar.
- Las cuentas inactivas se marcan como deshabilitadas y Spring Security rechaza su ingreso.
- Una cuenta con rol PROPIETARIO se vincula a un registro de `propietarios`, para filtrar sus datos en el Sprint 3.

## Consecuencias
- Agregar un módulo implica agregar su regla en `SecurityConfig` y su entrada en el menú de `fragments.html`.
- El mensaje de error del login es genérico para no revelar si el correo existe.
