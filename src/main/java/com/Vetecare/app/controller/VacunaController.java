package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.model.Vacuna;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.UsuarioRepository;
import com.Vetecare.app.repository.VacunaRepository;

import jakarta.validation.Valid;

/** HU-06: el veterinario registra vacunas y el sistema calcula la fecha de refuerzo. */
@Controller
@RequestMapping("/vacunas")
public class VacunaController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VacunaRepository vacunaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public VacunaController(VacunaRepository vacunaRepository, MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository) {
        this.vacunaRepository = vacunaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "false") boolean pendientes, Model model) {
        List<Vacuna> todas = vacunaRepository.findAllByOrderByFechaRefuerzoAsc();
        Set<Long> reforzadas = Vacuna.idsReforzadas(todas);
        model.addAttribute("vacunas", pendientes ? Vacuna.pendientes(todas) : todas);
        model.addAttribute("reforzadas", reforzadas);
        model.addAttribute("pendientes", pendientes);
        return "vacunas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long mascotaId, Model model) {
        Vacuna vacuna = new Vacuna();
        vacuna.setFechaAplicacion(LocalDate.now());
        vacuna.setIntervaloDias(365);
        if (mascotaId != null) {
            mascotaRepository.findById(mascotaId).ifPresent(vacuna::setMascota);
        }
        model.addAttribute("vacuna", vacuna);
        model.addAttribute("mascotas", mascotaRepository.findAllByOrderByNombreAsc());
        return "vacunas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("vacuna") Vacuna form, BindingResult result, Model model,
            Principal principal, RedirectAttributes ra) {
        Long mascotaId = form.getMascota() == null ? null : form.getMascota().getId();
        Mascota mascota = mascotaId == null ? null : mascotaRepository.findById(mascotaId).orElse(null);
        if (mascota == null) {
            result.rejectValue("mascota", "requerido", "Selecciona una mascota");
        }
        Usuario veterinario = principal == null ? null
                : usuarioRepository.findByCorreo(principal.getName()).orElse(null);
        if (veterinario == null) {
            result.reject("veterinario", "No se encontró tu cuenta de veterinario");
        }
        if (result.hasErrors()) {
            model.addAttribute("mascotas", mascotaRepository.findAllByOrderByNombreAsc());
            return "vacunas/formulario";
        }

        Vacuna vacuna = new Vacuna();
        vacuna.setMascota(mascota);
        vacuna.setNombre(form.getNombre());
        vacuna.setFechaAplicacion(form.getFechaAplicacion());
        vacuna.setIntervaloDias(form.getIntervaloDias());
        vacuna.setFechaRefuerzo(vacuna.calcularFechaRefuerzo());
        vacuna.setVeterinario(veterinario);
        vacunaRepository.save(vacuna);

        ra.addFlashAttribute("ok", "Vacuna registrada · refuerzo el " + vacuna.getFechaRefuerzo().format(FORMATO));
        return "redirect:/vacunas";
    }
}
