package com.Vetecare.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;

    public InicioController(UsuarioRepository usuarioRepository, PropietarioRepository propietarioRepository,
            MascotaRepository mascotaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalPropietarios", propietarioRepository.count());
        model.addAttribute("totalMascotas", mascotaRepository.count());
        return "inicio";
    }

    @RequestMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
