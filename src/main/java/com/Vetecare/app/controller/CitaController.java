package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.Vetecare.app.model.EstadoCita;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.Rol;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.CitaRepository;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.UsuarioRepository;

import jakarta.validation.Valid;

/** HU-04: la recepcionista agenda, modifica y cancela citas sin cruces ni fechas pasadas. */
@Controller
@RequestMapping("/citas")
public class CitaController {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaController(CitaRepository citaRepository, MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Agenda de un día, con filtros opcionales por veterinario y estado. */
    @GetMapping
    public String listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) EstadoCita estado, Model model) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        model.addAttribute("citas", citaRepository.buscar(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(),
                veterinarioId, estado));
        model.addAttribute("fecha", dia);
        model.addAttribute("veterinarioId", veterinarioId);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", EstadoCita.values());
        model.addAttribute("veterinarios", usuarioRepository.findByRolAndActivoTrueOrderByNombreAsc(Rol.VETERINARIO));
        return "citas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long mascotaId, Model model) {
        Cita cita = new Cita();
        if (mascotaId != null) {
            mascotaRepository.findById(mascotaId).ifPresent(cita::setMascota);
        }
        model.addAttribute("cita", cita);
        cargarOpciones(model);
        return "citas/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Cita cita = citaRepository.findById(id).orElse(null);
        if (cita == null || !cita.isProgramada()) {
            ra.addFlashAttribute("error", "Solo se pueden modificar citas programadas");
            return "redirect:/citas";
        }
        model.addAttribute("cita", cita);
        cargarOpciones(model);
        return "citas/formulario";
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardar(@Valid @ModelAttribute("cita") Cita form, BindingResult result, Model model,
            Principal principal, RedirectAttributes ra) {
        boolean esNueva = form.getId() == null;
        Cita cita = esNueva ? new Cita() : citaRepository.findById(form.getId()).orElse(null);
        if (cita == null || !cita.isProgramada()) {
            ra.addFlashAttribute("error", "Solo se pueden modificar citas programadas");
            return "redirect:/citas";
        }

        Long mascotaId = form.getMascota() == null ? null : form.getMascota().getId();
        Mascota mascota = mascotaId == null ? null : mascotaRepository.findById(mascotaId).orElse(null);
        if (mascota == null) {
            result.rejectValue("mascota", "requerido", "Selecciona una mascota");
        }

        Long veterinarioId = form.getVeterinario() == null ? null : form.getVeterinario().getId();
        Usuario veterinario = veterinarioId == null ? null : usuarioRepository.findById(veterinarioId)
                .filter(u -> u.getRol() == Rol.VETERINARIO && u.isActivo()).orElse(null);
        if (veterinario == null) {
            result.rejectValue("veterinario", "requerido", "Selecciona un veterinario");
        }

        LocalDateTime inicio = form.getFechaHora();
        if (inicio != null) {
            if (inicio.isBefore(LocalDateTime.now())) {
                // RN-03 / AC-015
                result.rejectValue("fechaHora", "pasada", "No se permiten fechas pasadas");
            } else if (veterinario != null
                    && citaRepository.veterinarioOcupado(veterinario.getId(), inicio, cita.getId())) {
                // RN-02 / AC-014
                result.rejectValue("fechaHora", "cruce", "El veterinario ya tiene una cita en ese horario");
            }
        }

        if (result.hasErrors()) {
            cargarOpciones(model);
            return "citas/formulario";
        }

        cita.setFechaHora(inicio);
        cita.setMotivo(form.getMotivo());
        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);
        if (esNueva) {
            cita.setEstado(EstadoCita.PROGRAMADA);
            cita.setRecepcionista(usuarioActual(principal));
        }
        citaRepository.save(cita);
        ra.addFlashAttribute("ok", esNueva ? "Cita agendada" : "Cita actualizada");
        return "redirect:/citas?fecha=" + inicio.toLocalDate();
    }

    /** La cita no se borra: queda como cancelada y libera el horario (AC-016). */
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        Cita cita = citaRepository.findById(id).orElse(null);
        if (cita == null || !cita.isProgramada()) {
            ra.addFlashAttribute("error", "Solo se pueden cancelar citas programadas");
            return "redirect:/citas";
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
        ra.addFlashAttribute("ok", "Cita cancelada");
        return "redirect:/citas?fecha=" + cita.getFechaHora().toLocalDate();
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("mascotas", mascotaRepository.findAllByOrderByNombreAsc());
        model.addAttribute("veterinarios", usuarioRepository.findByRolAndActivoTrueOrderByNombreAsc(Rol.VETERINARIO));
    }

    private Usuario usuarioActual(Principal principal) {
        return principal == null ? null : usuarioRepository.findByCorreo(principal.getName()).orElse(null);
    }
}
