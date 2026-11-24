package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Vetecare.app.model.Cita;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.CitaRepository;
import com.Vetecare.app.repository.UsuarioRepository;

/** El veterinario consulta sus citas del día. */
@Controller
public class AgendaController {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    public AgendaController(CitaRepository citaRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/agenda")
    public String agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Principal principal, Model model) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        Usuario veterinario = usuarioRepository.findByCorreo(principal.getName()).orElse(null);
        List<Cita> citas = veterinario == null ? List.of()
                : citaRepository.buscar(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(), veterinario.getId(), null, null);
        model.addAttribute("citas", citas);
        model.addAttribute("fecha", dia);
        return "agenda";
    }
}
