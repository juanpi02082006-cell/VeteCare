package com.Vetecare.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.Vetecare.app.config.DatosIniciales;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;

/** Pruebas de los criterios de aceptación del Sprint 1 (HU-01, HU-02 y HU-03). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:vetcaretest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class VetecareApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // AC-001
    @Test
    void loginConCredencialesValidasEntraConSuRol() throws Exception {
        mvc.perform(formLogin("/login").userParameter("correo")
                .user("admin@vetcare.com").password(DatosIniciales.CLAVE_DEMO))
            .andExpect(authenticated().withRoles("ADMIN"))
            .andExpect(redirectedUrl("/"));
    }

    // AC-002
    @Test
    void loginConContrasenaIncorrectaEsRechazado() throws Exception {
        mvc.perform(formLogin("/login").userParameter("correo")
                .user("admin@vetcare.com").password("incorrecta"))
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?error"));
    }

    // AC-003
    @Test
    void sinSesionSeRedirigeAlLogin() throws Exception {
        mvc.perform(get("/propietarios"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    // AC-004
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoPuedeEntrarAUsuarios() throws Exception {
        mvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }

    // AC-005
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaVeSoloSuMenu() throws Exception {
        mvc.perform(get("/propietarios"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("href=\"/mascotas\"")))
            .andExpect(content().string(not(containsString("href=\"/usuarios\""))));
    }

    // AC-006
    @Test
    void lasContrasenasSeGuardanCifradasConBCrypt() {
        Usuario admin = usuarioRepository.findByCorreo("admin@vetcare.com").orElseThrow();
        assertThat(admin.getPassword()).startsWith("$2").isNotEqualTo(DatosIniciales.CLAVE_DEMO);
        assertThat(passwordEncoder.matches(DatosIniciales.CLAVE_DEMO, admin.getPassword())).isTrue();
    }

    // AC-007
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void propietarioConDocumentoRepetidoNoSeGuarda() throws Exception {
        long antes = propietarioRepository.count();
        mvc.perform(post("/propietarios/guardar").with(csrf())
                .param("nombre", "Persona Repetida")
                .param("documento", "1098765432")
                .param("telefono", "3001112233"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("propietario", "documento"));
        assertThat(propietarioRepository.count()).isEqualTo(antes);
    }

    // AC-008
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void mascotaSinPropietarioNoSeGuarda() throws Exception {
        long antes = mascotaRepository.count();
        mvc.perform(post("/mascotas/guardar").with(csrf())
                .param("nombre", "Firulais")
                .param("especie", "PERRO")
                .param("sexo", "MACHO"))
            .andExpect(status().isOk())
            .andExpect(view().name("mascotas/formulario"))
            .andExpect(model().attributeHasFieldErrors("mascota", "propietario"));
        assertThat(mascotaRepository.count()).isEqualTo(antes);
    }

    // AC-009
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void mascotaConPropietarioExistenteSeGuarda() throws Exception {
        long antes = mascotaRepository.count();
        Long propietarioId = propietarioRepository.findAllByOrderByNombreAsc().get(0).getId();
        mvc.perform(post("/mascotas/guardar").with(csrf())
                .param("nombre", "Pelusa")
                .param("especie", "GATO")
                .param("sexo", "HEMBRA")
                .param("propietario.id", propietarioId.toString()))
            .andExpect(redirectedUrl("/mascotas"));
        assertThat(mascotaRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void lasPantallasCarganSinErrores() throws Exception {
        for (String ruta : new String[] { "/", "/usuarios", "/usuarios/nuevo", "/propietarios/nuevo",
                "/mascotas", "/mascotas/nuevo" }) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
    }
}
