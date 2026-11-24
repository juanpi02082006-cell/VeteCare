package com.Vetecare.app.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.Vetecare.app.model.CategoriaProducto;
import com.Vetecare.app.model.Cita;
import com.Vetecare.app.model.Consulta;
import com.Vetecare.app.model.DetalleConsulta;
import com.Vetecare.app.model.Especie;
import com.Vetecare.app.model.EstadoCita;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.MovimientoInventario;
import com.Vetecare.app.model.Producto;
import com.Vetecare.app.model.Propietario;
import com.Vetecare.app.model.Rol;
import com.Vetecare.app.model.Sexo;
import com.Vetecare.app.model.TipoMovimiento;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.model.Vacuna;
import com.Vetecare.app.repository.CitaRepository;
import com.Vetecare.app.repository.ConsultaRepository;
import com.Vetecare.app.repository.DetalleConsultaRepository;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.MovimientoInventarioRepository;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;
import com.Vetecare.app.repository.VacunaRepository;

/**
 * Datos ficticios de demostración (Ley 1581 de 2012: no se usan datos reales).
 * Cada sprint se carga por separado y solo si sus tablas están vacías.
 * Todas las cuentas de prueba usan la contraseña 12345.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    public static final String CLAVE_DEMO = "12345";

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final DetalleConsultaRepository detalleRepository;
    private final VacunaRepository vacunaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository, PropietarioRepository propietarioRepository,
            MascotaRepository mascotaRepository, CitaRepository citaRepository, ConsultaRepository consultaRepository,
            DetalleConsultaRepository detalleRepository, VacunaRepository vacunaRepository,
            ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.consultaRepository = consultaRepository;
        this.detalleRepository = detalleRepository;
        this.vacunaRepository = vacunaRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            cargarSprint1();
        }
        if (productoRepository.count() == 0 && citaRepository.count() == 0) {
            cargarSprint2();
        }
    }

    // ---------------------------------------------------------------- Sprint 1

    private void cargarSprint1() {
        Propietario carlos = propietario("Carlos Andrés Ruiz", "1098765432", "3001234567",
                "carlos.ruiz@correo.com", "Cra. 27 # 45-12, Bucaramanga");
        Propietario laura = propietario("Laura Patricia Gómez", "1095123456", "3109876543",
                "laura.gomez@correo.com", "Calle 56 # 30-21, Bucaramanga");
        Propietario jorge = propietario("Jorge Enrique Martínez", "91234567", "3157654321",
                "jorge.martinez@correo.com", "Av. Quebradaseca # 15-40, Bucaramanga");
        Propietario ana = propietario("Ana María Rodríguez", "1102345678", "3204567890",
                "ana.rodriguez@correo.com", "Cra. 33 # 52-08, Floridablanca");
        Propietario diego = propietario("Diego Alejandro Pérez", "1096789012", "3012345678",
                "diego.perez@correo.com", "Calle 105 # 22-14, Girón");
        propietarioRepository.saveAll(List.of(carlos, laura, jorge, ana, diego));

        mascotaRepository.saveAll(List.of(
                mascota("Max", Especie.PERRO, "Labrador Retriever", Sexo.MACHO, "2021-03-15", 28.5, carlos),
                mascota("Luna", Especie.GATO, "Siamés", Sexo.HEMBRA, "2022-07-02", 4.2, carlos),
                mascota("Rocky", Especie.PERRO, "Bulldog Francés", Sexo.MACHO, "2020-11-20", 12.8, laura),
                mascota("Mía", Especie.GATO, "Persa", Sexo.HEMBRA, "2019-05-10", 5.1, laura),
                mascota("Toby", Especie.PERRO, "Beagle", Sexo.MACHO, "2023-01-08", 11.3, jorge),
                mascota("Nala", Especie.PERRO, "Golden Retriever", Sexo.HEMBRA, "2018-09-25", 30.1, jorge),
                mascota("Simba", Especie.GATO, "Mestizo", Sexo.MACHO, "2024-02-14", 3.6, ana),
                mascota("Kira", Especie.PERRO, "Pastor Alemán", Sexo.HEMBRA, "2020-06-30", 32.4, ana),
                mascota("Coco", Especie.AVE, "Periquito australiano", Sexo.MACHO, "2023-08-12", 0.04, diego),
                mascota("Lola", Especie.PERRO, "Poodle", Sexo.HEMBRA, "2022-12-03", 6.7, diego)));

        usuarioRepository.saveAll(List.of(
                usuario("Administrador General", "admin@vetcare.com", Rol.ADMIN, null),
                usuario("Dra. Valentina Ortiz", "veterinario@vetcare.com", Rol.VETERINARIO, null),
                usuario("Sofía Herrera", "recepcion@vetcare.com", Rol.RECEPCIONISTA, null),
                usuario("Andrés Castillo", "inventario@vetcare.com", Rol.AUXILIAR, null),
                usuario("Carlos Andrés Ruiz", "carlos.ruiz@correo.com", Rol.PROPIETARIO, carlos)));
    }

    // ---------------------------------------------------------------- Sprint 2

    private void cargarSprint2() {
        Usuario valentina = usuarioRepository.findByCorreo("veterinario@vetcare.com").orElse(null);
        Usuario recepcion = usuarioRepository.findByCorreo("recepcion@vetcare.com").orElse(null);
        Usuario auxiliar = usuarioRepository.findByCorreo("inventario@vetcare.com").orElse(null);
        Map<String, Mascota> mascotas = new HashMap<>();
        mascotaRepository.findAllByOrderByNombreAsc().forEach(m -> mascotas.putIfAbsent(m.getNombre(), m));
        if (valentina == null || auxiliar == null || mascotas.size() < 10) {
            return; // los datos del Sprint 1 fueron modificados; no se fuerza la carga
        }
        Usuario mateo = usuarioRepository.findByCorreo("veterinario2@vetcare.com").orElseGet(() ->
                usuarioRepository.save(usuario("Dr. Mateo Rincón", "veterinario2@vetcare.com", Rol.VETERINARIO, null)));

        LocalDate hoy = LocalDate.now();

        // Inventario: 2 productos críticos, 1 próximo a vencer y 1 vencido
        Producto antirrabica = producto("Vacuna antirrábica", CategoriaProducto.VACUNA, 25000, 12, 5, hoy.plusDays(180), auxiliar);
        Producto polivalente = producto("Vacuna polivalente canina", CategoriaProducto.VACUNA, 45000, 15, 5, hoy.plusDays(200), auxiliar);
        Producto tripleFelina = producto("Vacuna triple felina", CategoriaProducto.VACUNA, 42000, 3, 4, hoy.plusDays(150), auxiliar);
        Producto omeprazol = producto("Omeprazol 20 mg (caja x 14)", CategoriaProducto.MEDICAMENTO, 18000, 10, 3, hoy.plusDays(365), auxiliar);
        Producto meloxicam = producto("Meloxicam 1,5 mg/ml", CategoriaProducto.MEDICAMENTO, 32000, 8, 2, hoy.plusDays(400), auxiliar);
        Producto pipeta = producto("Antiparasitario externo (pipeta)", CategoriaProducto.MEDICAMENTO, 22000, 20, 6, hoy.plusDays(300), auxiliar);
        Producto amoxicilina = producto("Amoxicilina 250 mg", CategoriaProducto.MEDICAMENTO, 15000, 7, 4, hoy.plusDays(18), auxiliar);
        Producto alimento = producto("Alimento cachorro 2 kg", CategoriaProducto.ALIMENTO, 68000, 3, 4, hoy.plusDays(120), auxiliar);
        producto("Jeringas 3 ml (x100)", CategoriaProducto.INSUMO, 30000, 40, 10, hoy.plusDays(700), auxiliar);
        producto("Suero fisiológico 500 ml", CategoriaProducto.INSUMO, 9000, 6, 3, hoy.minusDays(5), auxiliar);
        salida(alimento, 1, "Venta en mostrador", auxiliar, hoy.minusDays(4).atTime(11, 20));

        // Citas: 8 atendidas con su consulta, 1 cancelada y 3 programadas
        atendida(mascotas.get("Max"), valentina, recepcion, hoy.minusDays(20), 9, 0, "Control anual",
                "Paciente sano, buena condición corporal", "Continuar dieta actual; refuerzo de vacunas", 28.9, null, 0);
        atendida(mascotas.get("Luna"), mateo, recepcion, hoy.minusDays(18), 10, 30, "Vómito ocasional",
                "Gastritis leve", "Omeprazol por 5 días y dieta blanda", 4.1, omeprazol, 1);
        atendida(mascotas.get("Rocky"), valentina, recepcion, hoy.minusDays(15), 11, 0, "Dificultad respiratoria",
                "Síndrome braquicefálico leve", "Evitar ejercicio en horas de calor; control en 30 días", 13.0, null, 0);
        atendida(mascotas.get("Mía"), mateo, recepcion, hoy.minusDays(12), 15, 0, "Caída de pelo",
                "Dermatitis por pulgas", "Antiparasitario externo y baño medicado semanal", 5.0, pipeta, 1);
        atendida(mascotas.get("Toby"), valentina, recepcion, hoy.minusDays(10), 8, 30, "Vacunación",
                "Paciente apto para vacunación", "Aplicación de vacuna polivalente", 11.5, polivalente, 1);
        atendida(mascotas.get("Nala"), mateo, recepcion, hoy.minusDays(7), 16, 0, "Cojera en pata trasera",
                "Displasia de cadera leve", "Meloxicam por 7 días y control de peso", 30.4, meloxicam, 1);
        atendida(mascotas.get("Kira"), valentina, recepcion, hoy.minusDays(5), 9, 30, "Otitis",
                "Otitis externa bacteriana", "Limpieza ótica y gotas antibióticas por 10 días", 32.0, null, 0);
        atendida(mascotas.get("Coco"), mateo, recepcion, hoy.minusDays(3), 14, 0, "Plumas erizadas",
                "Estrés por cambio de ambiente", "Enriquecimiento ambiental; control en 15 días", 0.04, null, 0);
        cita(mascotas.get("Rocky"), valentina, recepcion, hoy.plusDays(2).atTime(10, 0), "Baño medicado", EstadoCita.CANCELADA);
        cita(mascotas.get("Simba"), valentina, recepcion, hoy.atTime(15, 0), "Primera vacuna", EstadoCita.PROGRAMADA);
        cita(mascotas.get("Lola"), valentina, recepcion, hoy.atTime(16, 30), "Revisión dental", EstadoCita.PROGRAMADA);
        cita(mascotas.get("Max"), mateo, recepcion, hoy.plusDays(1).atTime(9, 0), "Control de peso", EstadoCita.PROGRAMADA);

        // Vacunas: refuerzos vencidos, próximos, al día y uno ya reforzado (Nala)
        vacuna(mascotas.get("Max"), "Rabia", hoy.minusDays(340), 365, valentina);
        vacuna(mascotas.get("Max"), "Polivalente canina", hoy.minusDays(355), 365, valentina);
        vacuna(mascotas.get("Luna"), "Triple felina", hoy.minusDays(400), 365, mateo);
        vacuna(mascotas.get("Rocky"), "Rabia", hoy.minusDays(200), 365, valentina);
        vacuna(mascotas.get("Mía"), "Triple felina", hoy.minusDays(370), 365, mateo);
        vacuna(mascotas.get("Toby"), "Polivalente canina", hoy.minusDays(10), 365, valentina);
        vacuna(mascotas.get("Nala"), "Rabia", hoy.minusDays(380), 365, mateo);
        vacuna(mascotas.get("Nala"), "Rabia", hoy.minusDays(7), 365, mateo);
        vacuna(mascotas.get("Kira"), "Leptospirosis", hoy.minusDays(175), 180, valentina);
        vacuna(mascotas.get("Lola"), "Bordetella", hoy.minusDays(170), 180, valentina);
    }

    // ---------------------------------------------------------------- Ayudantes

    private Propietario propietario(String nombre, String documento, String telefono, String correo,
            String direccion) {
        Propietario p = new Propietario();
        p.setNombre(nombre);
        p.setDocumento(documento);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        p.setDireccion(direccion);
        return p;
    }

    private Mascota mascota(String nombre, Especie especie, String raza, Sexo sexo, String nacimiento,
            double peso, Propietario propietario) {
        Mascota m = new Mascota();
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza(raza);
        m.setSexo(sexo);
        m.setFechaNacimiento(LocalDate.parse(nacimiento));
        m.setPeso(peso);
        m.setPropietario(propietario);
        return m;
    }

    private Usuario usuario(String nombre, String correo, Rol rol, Propietario propietario) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(CLAVE_DEMO));
        u.setRol(rol);
        u.setPropietario(propietario);
        return u;
    }

    /** Crea el producto con su entrada de stock inicial hace 30 días. */
    private Producto producto(String nombre, CategoriaProducto categoria, int precio, int stock, int stockMinimo,
            LocalDate vencimiento, Usuario auxiliar) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecio(BigDecimal.valueOf(precio));
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setFechaVencimiento(vencimiento);
        productoRepository.save(p);
        movimiento(p, TipoMovimiento.ENTRADA, stock, "Stock inicial", auxiliar,
                LocalDate.now().minusDays(30).atTime(LocalTime.of(8, 0)));
        return p;
    }

    private void salida(Producto producto, int cantidad, String motivo, Usuario usuario, LocalDateTime fecha) {
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
        movimiento(producto, TipoMovimiento.SALIDA, cantidad, motivo, usuario, fecha);
    }

    private void movimiento(Producto producto, TipoMovimiento tipo, int cantidad, String motivo, Usuario usuario,
            LocalDateTime fecha) {
        MovimientoInventario m = new MovimientoInventario();
        m.setProducto(producto);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setMotivo(motivo);
        m.setUsuario(usuario);
        m.setFechaMovimiento(fecha);
        movimientoRepository.save(m);
    }

    private Cita cita(Mascota mascota, Usuario veterinario, Usuario recepcionista, LocalDateTime fechaHora,
            String motivo, EstadoCita estado) {
        Cita c = new Cita();
        c.setMascota(mascota);
        c.setVeterinario(veterinario);
        c.setRecepcionista(recepcionista);
        c.setFechaHora(fechaHora);
        c.setMotivo(motivo);
        c.setEstado(estado);
        return citaRepository.save(c);
    }

    /** Cita atendida con su consulta y, si aplica, un producto usado (que descuenta stock). */
    private void atendida(Mascota mascota, Usuario veterinario, Usuario recepcionista, LocalDate dia, int hora,
            int minuto, String motivo, String diagnostico, String tratamiento, double peso, Producto producto,
            int cantidad) {
        LocalDateTime fechaHora = dia.atTime(hora, minuto);
        Cita c = cita(mascota, veterinario, recepcionista, fechaHora, motivo, EstadoCita.ATENDIDA);
        Consulta consulta = new Consulta();
        consulta.setCita(c);
        consulta.setDiagnostico(diagnostico);
        consulta.setTratamiento(tratamiento);
        consulta.setPeso(peso);
        consulta.setFechaRegistro(fechaHora.plusMinutes(25));
        consultaRepository.save(consulta);
        if (producto != null) {
            DetalleConsulta d = new DetalleConsulta();
            d.setConsulta(consulta);
            d.setProducto(producto);
            d.setCantidad(cantidad);
            detalleRepository.save(d);
            salida(producto, cantidad, "Consulta #" + consulta.getId(), veterinario, fechaHora.plusMinutes(25));
        }
    }

    private void vacuna(Mascota mascota, String nombre, LocalDate aplicacion, int intervaloDias, Usuario veterinario) {
        Vacuna v = new Vacuna();
        v.setMascota(mascota);
        v.setNombre(nombre);
        v.setFechaAplicacion(aplicacion);
        v.setIntervaloDias(intervaloDias);
        v.setFechaRefuerzo(v.calcularFechaRefuerzo());
        v.setVeterinario(veterinario);
        vacunaRepository.save(v);
    }
}
