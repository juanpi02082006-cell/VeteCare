package com.Vetecare.app.controller;

import java.time.LocalDate;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Vetecare.app.model.EstadoCita;
import com.Vetecare.app.model.Vacuna;
import com.Vetecare.app.repository.CitaRepository;
import com.Vetecare.app.repository.ConsultaRepository;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;
import com.Vetecare.app.repository.VacunaRepository;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final VacunaRepository vacunaRepository;
    private final ProductoRepository productoRepository;

    public InicioController(UsuarioRepository usuarioRepository, PropietarioRepository propietarioRepository,
            MascotaRepository mascotaRepository, CitaRepository citaRepository, ConsultaRepository consultaRepository,
            VacunaRepository vacunaRepository, ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.consultaRepository = consultaRepository;
        this.vacunaRepository = vacunaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Sin sesión se muestra la landing; con sesión, el panel de inicio con los números de su rol. */
    @GetMapping("/")
    public String inicio(Authentication autenticacion, Model model) {
        if (autenticacion == null || autenticacion instanceof AnonymousAuthenticationToken) {
            return "landing";
        }
        var desde = LocalDate.now().atStartOfDay();
        var hasta = desde.plusDays(1).minusNanos(1);

        if (tieneRol(autenticacion, "ADMIN")) {
            model.addAttribute("totalUsuarios", usuarioRepository.count());
        }
        if (tieneRol(autenticacion, "ADMIN") || tieneRol(autenticacion, "RECEPCIONISTA")) {
            model.addAttribute("totalPropietarios", propietarioRepository.count());
            model.addAttribute("totalMascotas", mascotaRepository.count());
            model.addAttribute("citasHoy", citaRepository.countByFechaHoraBetweenAndEstado(desde, hasta,
                    EstadoCita.PROGRAMADA));
        }
        if (tieneRol(autenticacion, "VETERINARIO")) {
            usuarioRepository.findByCorreo(autenticacion.getName()).ifPresent(vet -> {
                model.addAttribute("misCitasHoy", citaRepository.countByVeterinarioIdAndFechaHoraBetweenAndEstado(
                        vet.getId(), desde, hasta, EstadoCita.PROGRAMADA));
                model.addAttribute("misConsultas", consultaRepository.countByCitaVeterinarioId(vet.getId()));
            });
            model.addAttribute("vacunasPendientes",
                    Vacuna.pendientes(vacunaRepository.findAllByOrderByFechaRefuerzoAsc()).size());
        }
        if (tieneRol(autenticacion, "AUXILIAR")) {
            model.addAttribute("totalProductos", productoRepository.count());
        }
        return "inicio";
    }

    @RequestMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }

    private boolean tieneRol(Authentication autenticacion, String rol) {
        return autenticacion.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
