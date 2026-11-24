package com.Vetecare.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.Vetecare.app.model.CategoriaProducto;
import com.Vetecare.app.model.Cita;
import com.Vetecare.app.model.Consulta;
import com.Vetecare.app.model.EstadoCita;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.Producto;
import com.Vetecare.app.model.TipoMovimiento;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.model.Vacuna;
import com.Vetecare.app.repository.CitaRepository;
import com.Vetecare.app.repository.ConsultaRepository;
import com.Vetecare.app.repository.DetalleConsultaRepository;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.MovimientoInventarioRepository;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.UsuarioRepository;
import com.Vetecare.app.repository.VacunaRepository;

/** Pruebas de los criterios de aceptación del Sprint 2 (HU-04 a HU-08). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:vetcaretest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class Sprint2Tests {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final String VETERINARIA = "veterinario@vetcare.com";
    private static final String AUXILIAR = "inventario@vetcare.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private DetalleConsultaRepository detalleRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    // ------------------------------------------------------------------ HU-04 · Citas

    // AC-014
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void citaQueSeCruzaConOtraDelMismoVeterinarioSeRechaza() throws Exception {
        LocalDateTime diez = LocalDate.now().plusDays(30).atTime(10, 0);
        nuevaCita(diez, EstadoCita.PROGRAMADA);
        long antes = citaRepository.count();

        mvc.perform(post("/citas/guardar").with(csrf())
                .param("mascota.id", mascota("Toby").getId().toString())
                .param("veterinario.id", valentina().getId().toString())
                .param("fechaHora", diez.plusMinutes(15).format(FECHA_HORA))
                .param("motivo", "Control"))
            .andExpect(status().isOk())
            .andExpect(view().name("citas/formulario"))
            .andExpect(model().attributeHasFieldErrorCode("cita", "fechaHora", "cruce"));
        assertThat(citaRepository.count()).isEqualTo(antes);
    }

    // AC-015
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void citaEnFechaPasadaSeRechaza() throws Exception {
        long antes = citaRepository.count();
        mvc.perform(post("/citas/guardar").with(csrf())
                .param("mascota.id", mascota("Toby").getId().toString())
                .param("veterinario.id", valentina().getId().toString())
                .param("fechaHora", LocalDateTime.now().minusDays(1).format(FECHA_HORA))
                .param("motivo", "Control"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrorCode("cita", "fechaHora", "pasada"));
        assertThat(citaRepository.count()).isEqualTo(antes);
    }

    // AC-016
    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void cancelarUnaCitaLiberaElHorario() throws Exception {
        LocalDateTime hora = LocalDate.now().plusDays(31).atTime(10, 0);
        Cita cita = nuevaCita(hora, EstadoCita.PROGRAMADA);

        mvc.perform(post("/citas/{id}/cancelar", cita.getId()).with(csrf()))
            .andExpect(status().is3xxRedirection());
        assertThat(citaRepository.findById(cita.getId()).orElseThrow().getEstado()).isEqualTo(EstadoCita.CANCELADA);

        long antes = citaRepository.count();
        mvc.perform(post("/citas/guardar").with(csrf())
                .param("mascota.id", mascota("Toby").getId().toString())
                .param("veterinario.id", valentina().getId().toString())
                .param("fechaHora", hora.format(FECHA_HORA))
                .param("motivo", "Control"))
            .andExpect(status().is3xxRedirection());
        assertThat(citaRepository.count()).isEqualTo(antes + 1);
    }

    // ------------------------------------------------------------------ HU-05 · Consultas

    // AC-017
    @Test
    @WithUserDetails(VETERINARIA)
    void segundaConsultaParaLaMismaCitaSeRechaza() throws Exception {
        Consulta existente = consultaRepository.deVeterinario(valentina().getId()).get(0);
        long antes = consultaRepository.count();

        mvc.perform(post("/consultas/guardar").with(csrf())
                .param("cita.id", existente.getCita().getId().toString())
                .param("diagnostico", "Repetida")
                .param("tratamiento", "Repetido"))
            .andExpect(status().isOk())
            .andExpect(view().name("consultas/formulario"))
            .andExpect(content().string(containsString("Esta cita ya tiene una consulta")));
        assertThat(consultaRepository.count()).isEqualTo(antes);
    }

    // AC-018
    @Test
    @WithUserDetails(VETERINARIA)
    void consultaParaUnaCitaCanceladaSeRechaza() throws Exception {
        Cita cancelada = nuevaCita(LocalDate.now().plusDays(32).atTime(9, 0), EstadoCita.CANCELADA);
        long antes = consultaRepository.count();

        mvc.perform(post("/consultas/guardar").with(csrf())
                .param("cita.id", cancelada.getId().toString())
                .param("diagnostico", "Sano")
                .param("tratamiento", "Ninguno"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("La cita está cancelada")));
        assertThat(consultaRepository.count()).isEqualTo(antes);
    }

    @Test
    @WithUserDetails(VETERINARIA)
    void consultaValidaMarcaLaCitaComoAtendidaYActualizaElPeso() throws Exception {
        Cita cita = nuevaCita(LocalDate.now().plusDays(33).atTime(9, 0), EstadoCita.PROGRAMADA);

        mvc.perform(post("/consultas/guardar").with(csrf())
                .param("cita.id", cita.getId().toString())
                .param("diagnostico", "Paciente sano")
                .param("tratamiento", "Ninguno")
                .param("peso", "11.9"))
            .andExpect(status().is3xxRedirection());

        assertThat(consultaRepository.existsByCitaId(cita.getId())).isTrue();
        assertThat(citaRepository.findById(cita.getId()).orElseThrow().getEstado()).isEqualTo(EstadoCita.ATENDIDA);
        assertThat(mascotaRepository.findById(cita.getMascota().getId()).orElseThrow().getPeso()).isEqualTo(11.9);
    }

    // RN-08 / REQ-VET-022
    @Test
    @WithUserDetails(VETERINARIA)
    void productoVencidoNoSePuedeUsarEnUnaConsulta() throws Exception {
        Consulta consulta = consultaRepository.deVeterinario(valentina().getId()).get(0);
        Producto vencido = nuevoProducto("Vencido de prueba", 10, 2, LocalDate.now().minusDays(1));
        long detallesAntes = detalleRepository.count();

        mvc.perform(post("/consultas/{id}/productos", consulta.getId()).with(csrf())
                .param("productoId", vencido.getId().toString())
                .param("cantidad", "1"))
            .andExpect(flash().attribute("error", "No se pueden usar productos vencidos"));
        assertThat(detalleRepository.count()).isEqualTo(detallesAntes);
        assertThat(productoRepository.findById(vencido.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    @WithUserDetails(VETERINARIA)
    void productoUsadoEnConsultaDescuentaStockYRegistraSalida() throws Exception {
        Consulta consulta = consultaRepository.deVeterinario(valentina().getId()).get(0);
        Producto producto = nuevoProducto("Gasas de prueba", 10, 2, LocalDate.now().plusDays(200));

        mvc.perform(post("/consultas/{id}/productos", consulta.getId()).with(csrf())
                .param("productoId", producto.getId().toString())
                .param("cantidad", "3"))
            .andExpect(flash().attribute("ok", "Producto agregado"));
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(7);
        assertThat(movimientoRepository.findByProductoIdOrderByFechaMovimientoDescIdDesc(producto.getId()))
            .singleElement().satisfies(m -> assertThat(m.getTipo()).isEqualTo(TipoMovimiento.SALIDA));
    }

    // ------------------------------------------------------------------ HU-06 · Vacunas

    // AC-019
    @Test
    void laFechaDeRefuerzoSumaElIntervaloALaAplicacion() {
        Vacuna vacuna = new Vacuna();
        vacuna.setFechaAplicacion(LocalDate.of(2026, 11, 20));
        vacuna.setIntervaloDias(365);
        assertThat(vacuna.calcularFechaRefuerzo()).isEqualTo(LocalDate.of(2027, 11, 20));
    }

    // AC-019 (de punta a punta)
    @Test
    @WithUserDetails(VETERINARIA)
    void alRegistrarUnaVacunaSeGuardaSuFechaDeRefuerzo() throws Exception {
        Mascota simba = mascota("Simba");
        LocalDate hoy = LocalDate.now();
        mvc.perform(post("/vacunas/guardar").with(csrf())
                .param("mascota.id", simba.getId().toString())
                .param("nombre", "Vacuna de prueba")
                .param("fechaAplicacion", hoy.toString())
                .param("intervaloDias", "21"))
            .andExpect(status().is3xxRedirection());
        assertThat(vacunaRepository.findByMascotaIdOrderByFechaAplicacionDesc(simba.getId()))
            .filteredOn(v -> v.getNombre().equals("Vacuna de prueba"))
            .singleElement()
            .satisfies(v -> assertThat(v.getFechaRefuerzo()).isEqualTo(hoy.plusDays(21)));
    }

    // ------------------------------------------------------------------ HU-07 · Inventario

    // AC-020
    @Test
    @WithUserDetails(AUXILIAR)
    void salidaMayorAlStockSeRechazaYElStockNoCambia() throws Exception {
        Producto producto = nuevoProducto("Producto AC-020", 5, 2, null);
        mvc.perform(post("/inventario/movimientos/guardar").with(csrf())
                .param("producto.id", producto.getId().toString())
                .param("tipo", "SALIDA")
                .param("cantidad", "8"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrorCode("movimiento", "cantidad", "stock"));
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(5);
        assertThat(movimientoRepository.existsByProductoId(producto.getId())).isFalse();
    }

    // AC-021
    @Test
    @WithUserDetails(AUXILIAR)
    void entradaSumaAlStockYQuedaRegistrada() throws Exception {
        Producto producto = nuevoProducto("Producto AC-021", 5, 2, null);
        mvc.perform(post("/inventario/movimientos/guardar").with(csrf())
                .param("producto.id", producto.getId().toString())
                .param("tipo", "ENTRADA")
                .param("cantidad", "10")
                .param("motivo", "Compra"))
            .andExpect(status().is3xxRedirection());
        assertThat(productoRepository.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(15);
        assertThat(movimientoRepository.findByProductoIdOrderByFechaMovimientoDescIdDesc(producto.getId()))
            .singleElement().satisfies(m -> {
                assertThat(m.getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
                assertThat(m.getUsuario().getCorreo()).isEqualTo(AUXILIAR);
            });
    }

    // ------------------------------------------------------------------ HU-08 · Alertas

    // AC-022
    @Test
    void productoConStockIgualAlMinimoEsCritico() {
        Producto producto = new Producto();
        producto.setStock(3);
        producto.setStockMinimo(3);
        assertThat(producto.estaCritico()).isTrue();
        producto.setStock(4);
        assertThat(producto.estaCritico()).isFalse();
    }

    // AC-023
    @Test
    void productoQueVenceDentroDe30DiasEstaPorVencer() {
        Producto producto = new Producto();
        producto.setFechaVencimiento(LocalDate.now().plusDays(16));
        assertThat(producto.estaPorVencer()).isTrue();
        producto.setFechaVencimiento(LocalDate.now().plusDays(31));
        assertThat(producto.estaPorVencer()).isFalse();
        producto.setFechaVencimiento(LocalDate.now().minusDays(1));
        assertThat(producto.estaVencido()).isTrue();
        assertThat(producto.estaPorVencer()).isFalse();
    }

    // AC-022 y AC-023 en pantalla, con el contador del menú
    @Test
    @WithUserDetails(AUXILIAR)
    void laPantallaDeAlertasMuestraLosProductosDeLosDatosDePrueba() throws Exception {
        mvc.perform(get("/inventario/alertas"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Vacuna triple felina")))
            .andExpect(content().string(containsString("Amoxicilina 250 mg")))
            .andExpect(content().string(containsString("Suero fisiológico 500 ml")))
            .andExpect(content().string(containsString("vc-nav-count")));
    }

    // ------------------------------------------------------------------ Acceso y pantallas

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void recepcionistaNoEntraAlInventario() throws Exception {
        mvc.perform(get("/inventario/productos")).andExpect(status().isForbidden());
        mvc.perform(get("/agenda")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AUXILIAR")
    void auxiliarNoEntraACitasNiConsultas() throws Exception {
        mvc.perform(get("/citas")).andExpect(status().isForbidden());
        mvc.perform(get("/consultas")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void pantallasDeCitasCargan() throws Exception {
        Cita cita = nuevaCita(LocalDate.now().plusDays(34).atTime(9, 0), EstadoCita.PROGRAMADA);
        for (String ruta : new String[] { "/", "/citas", "/citas?fecha=" + LocalDate.now().plusDays(34),
                "/citas/nueva", "/citas/" + cita.getId() + "/editar" }) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
    }

    @Test
    @WithUserDetails(VETERINARIA)
    void pantallasDelVeterinarioCargan() throws Exception {
        Cita cita = nuevaCita(LocalDate.now().plusDays(35).atTime(9, 0), EstadoCita.PROGRAMADA);
        Consulta consulta = consultaRepository.deVeterinario(valentina().getId()).get(0);
        for (String ruta : new String[] { "/", "/agenda", "/consultas", "/consultas/" + consulta.getId(),
                "/consultas/nueva?citaId=" + cita.getId(), "/consultas/historial/" + mascota("Nala").getId(),
                "/vacunas", "/vacunas?pendientes=true", "/vacunas/nueva" }) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
    }

    @Test
    @WithUserDetails(AUXILIAR)
    void pantallasDelInventarioCargan() throws Exception {
        Long productoId = productoRepository.findAllByOrderByNombreAsc().get(0).getId();
        for (String ruta : new String[] { "/", "/inventario/productos", "/inventario/productos/nuevo",
                "/inventario/productos/" + productoId + "/editar", "/inventario/movimientos",
                "/inventario/movimientos?productoId=" + productoId, "/inventario/movimientos/nuevo?tipo=SALIDA" }) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void filtrosDeCitasPorVistaTextoYEstado() throws Exception {
        for (String vista : new String[] { "dia", "semana", "proximas" }) {
            mvc.perform(get("/citas").param("vista", vista)).andExpect(status().isOk());
        }
        // Búsqueda: solo aparecen las citas de Lola (programada hoy en los datos de prueba)
        mvc.perform(get("/citas").param("vista", "proximas").param("q", "lola"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(">Lola<")))
            .andExpect(content().string(not(containsString(">Max<"))));
        // Estado: la pestaña de canceladas muestra la cita cancelada de Rocky y no las programadas
        mvc.perform(get("/citas").param("vista", "proximas").param("estado", "CANCELADA"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(">Rocky<")))
            .andExpect(content().string(not(containsString(">Lola<"))));
    }

    // ------------------------------------------------------------------ Ayudantes

    private Usuario valentina() {
        return usuarioRepository.findByCorreo(VETERINARIA).orElseThrow();
    }

    private Mascota mascota(String nombre) {
        return mascotaRepository.findAllByOrderByNombreAsc().stream()
                .filter(m -> m.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Cita nuevaCita(LocalDateTime fechaHora, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setMascota(mascota("Toby"));
        cita.setVeterinario(valentina());
        cita.setFechaHora(fechaHora);
        cita.setMotivo("Prueba");
        cita.setEstado(estado);
        return citaRepository.save(cita);
    }

    private Producto nuevoProducto(String nombre, int stock, int stockMinimo, LocalDate vencimiento) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoria(CategoriaProducto.INSUMO);
        producto.setPrecio(BigDecimal.valueOf(1000));
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setFechaVencimiento(vencimiento);
        return productoRepository.save(producto);
    }
}
