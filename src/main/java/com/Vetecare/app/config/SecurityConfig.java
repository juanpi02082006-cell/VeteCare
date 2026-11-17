package com.Vetecare.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.Vetecare.app.repository.UsuarioRepository;

/**
 * Acceso por rol (REQ-VET-001 a REQ-VET-004).
 * Las rutas de módulos futuros se agregan aquí en cada sprint.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Carga la cuenta por correo; las cuentas inactivas no pueden iniciar sesión. */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return correo -> usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .map(u -> User.withUsername(u.getCorreo())
                        .password(u.getPassword())
                        .roles(u.getRol().name())
                        .disabled(!u.isActivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/usuarios/**", "/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/propietarios/**", "/mascotas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("correo")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?salir")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
            // Solo para la consola de H2 en desarrollo
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
