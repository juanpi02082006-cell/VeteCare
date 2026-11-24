package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Vetecare.app.model.Cita;
import com.Vetecare.app.model.Consulta;
import com.Vetecare.app.model.DetalleConsulta;
import com.Vetecare.app.model.EstadoCita;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.MovimientoInventario;
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

import jakarta.validation.Valid;

/** HU-05: el veterinario registra consultas asociadas a una cita válida. */
@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaRepository consultaRepository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final DetalleConsultaRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final VacunaRepository vacunaRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsultaController(ConsultaRepository consultaRepository, CitaRepository citaRepository,
            MascotaRepository mascotaRepository, DetalleConsultaRepository detalleRepository,
            ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository,
            VacunaRepository vacunaRepository, UsuarioRepository usuarioRepository) {
        this.consultaRepository = consultaRepository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.vacunaRepository = vacunaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(Principal principal, Model model) {
        Usuario veterinario = usuarioActual(principal);
        model.addAttribute("consultas",
                veterinario == null ? List.of() : consultaRepository.deVeterinario(veterinario.getId()));
        return "consultas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam Long citaId, Principal principal, Model model, RedirectAttributes ra) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        String error = validarCita(cita, usuarioActual(principal));
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/agenda";
        }
        Consulta consulta = new Consulta();
        consulta.setCita(cita);
        consulta.setPeso(cita.getMascota().getPeso());
        model.addAttribute("consulta", consulta);
        return "consultas/formulario";
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardar(@Valid @ModelAttribute("consulta") Consulta form, BindingResult result,
            Principal principal, RedirectAttributes ra) {
        Long citaId = form.getCita() == null ? null : form.getCita().getId();
        Cita cita = citaId == null ? null : citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            ra.addFlashAttribute("error", "La cita no existe");
            return "redirect:/agenda";
        }
        form.setCita(cita);

        String error = validarCita(cita, usuarioActual(principal));
        if (error != null) {
            result.reject("cita", error);
        }
        if (result.hasErrors()) {
            return "consultas/formulario";
        }

        Consulta consulta = new Consulta();
        consulta.setCita(cita);
        consulta.setDiagnostico(form.getDiagnostico());
        consulta.setTratamiento(form.getTratamiento());
        consulta.setPeso(form.getPeso());
        consulta.setObservaciones(form.getObservaciones());
        consulta.setFechaRegistro(LocalDateTime.now());
        consultaRepository.save(consulta);

        cita.setEstado(EstadoCita.ATENDIDA);
        citaRepository.save(cita);

        // El peso registrado en la consulta pasa a ser el peso actual de la mascota
        if (form.getPeso() != null) {
            Mascota mascota = cita.getMascota();
            mascota.setPeso(form.getPeso());
            mascotaRepository.save(mascota);
        }

        ra.addFlashAttribute("ok", "Consulta registrada");
        return "redirect:/consultas/" + consulta.getId();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Principal principal, Model model, RedirectAttributes ra) {
        Consulta consulta = consultaRepository.findById(id).orElse(null);
        if (consulta == null) {
            ra.addFlashAttribute("error", "La consulta no existe");
            return "redirect:/consultas";
        }
        Usuario veterinario = usuarioActual(principal);
        model.addAttribute("consulta", consulta);
        model.addAttribute("detalles", detalleRepository.findByConsultaIdOrderByIdAsc(id));
        model.addAttribute("productos", productoRepository.disponibles(LocalDate.now()));
        model.addAttribute("puedeAgregar", esSuCita(consulta.getCita(), veterinario));
        return "consultas/detalle";
    }

    /** Desde la agenda: abre la consulta de una cita atendida. */
    @GetMapping("/cita/{citaId}")
    public String deCita(@PathVariable Long citaId, RedirectAttributes ra) {
        return consultaRepository.findByCitaId(citaId)
                .map(c -> "redirect:/consultas/" + c.getId())
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Esa cita no tiene consulta");
                    return "redirect:/agenda";
                });
    }

    /** Agrega un producto usado: valida RN-08 y RN-07, descuenta el stock y registra la salida. */
    @PostMapping("/{id}/productos")
    @Transactional
    public String agregarProducto(@PathVariable Long id, @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Integer cantidad, Principal principal, RedirectAttributes ra) {
        Consulta consulta = consultaRepository.findById(id).orElse(null);
        if (consulta == null) {
            ra.addFlashAttribute("error", "La consulta no existe");
            return "redirect:/consultas";
        }
        Usuario veterinario = usuarioActual(principal);
        Producto producto = productoId == null ? null : productoRepository.findById(productoId).orElse(null);

        String error = null;
        if (!esSuCita(consulta.getCita(), veterinario)) {
            error = "Solo el veterinario de la consulta puede agregar productos";
        } else if (producto == null) {
            error = "Selecciona un producto";
        } else if (cantidad == null || cantidad < 1) {
            error = "La cantidad debe ser al menos 1";
        } else if (producto.estaVencido()) {
            error = "No se pueden usar productos vencidos";
        } else if (cantidad > producto.getStock()) {
            error = "Stock insuficiente: hay " + producto.getStock() + " unidades de " + producto.getNombre();
        }
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/consultas/" + id;
        }

        DetalleConsulta detalle = new DetalleConsulta();
        detalle.setConsulta(consulta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalleRepository.save(detalle);

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        MovimientoInventario salida = new MovimientoInventario();
        salida.setProducto(producto);
        salida.setUsuario(veterinario);
        salida.setTipo(TipoMovimiento.SALIDA);
        salida.setCantidad(cantidad);
        salida.setMotivo("Consulta #" + consulta.getId());
        salida.setFechaMovimiento(LocalDateTime.now());
        movimientoRepository.save(salida);

        ra.addFlashAttribute("ok", "Producto agregado");
        return "redirect:/consultas/" + id;
    }

    /** Antecedentes de la mascota: consultas y vacunas. */
    @GetMapping("/historial/{mascotaId}")
    public String historial(@PathVariable Long mascotaId, Model model, RedirectAttributes ra) {
        Mascota mascota = mascotaRepository.findById(mascotaId).orElse(null);
        if (mascota == null) {
            ra.addFlashAttribute("error", "La mascota no existe");
            return "redirect:/agenda";
        }
        List<Vacuna> vacunas = vacunaRepository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId);
        model.addAttribute("mascota", mascota);
        model.addAttribute("consultas", consultaRepository.deMascota(mascotaId));
        model.addAttribute("vacunas", vacunas);
        model.addAttribute("reforzadas", Vacuna.idsReforzadas(vacunas));
        return "consultas/historial";
    }

    /** RN-04: la cita debe existir, no estar cancelada, no tener consulta y ser del veterinario. */
    private String validarCita(Cita cita, Usuario veterinario) {
        if (cita == null) {
            return "La cita no existe";
        }
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            return "La cita está cancelada";
        }
        if (consultaRepository.existsByCitaId(cita.getId())) {
            return "Esta cita ya tiene una consulta";
        }
        if (!esSuCita(cita, veterinario)) {
            return "La cita está asignada a otro veterinario";
        }
        return null;
    }

    private boolean esSuCita(Cita cita, Usuario veterinario) {
        return veterinario != null && cita.getVeterinario().getId().equals(veterinario.getId());
    }

    private Usuario usuarioActual(Principal principal) {
        return principal == null ? null : usuarioRepository.findByCorreo(principal.getName()).orElse(null);
    }
}
