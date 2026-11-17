package com.Vetecare.app.controller;

import java.security.Principal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Vetecare.app.model.Propietario;
import com.Vetecare.app.model.Rol;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;

import jakarta.validation.Valid;

/** HU-01: el administrador crea usuarios y les asigna uno de los cinco roles. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final int CLAVE_MINIMA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PropietarioRepository propietarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAllByOrderByNombreAsc());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        cargarOpciones(model);
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setPassword(null);
            model.addAttribute("usuario", usuario);
            cargarOpciones(model);
            return "usuarios/formulario";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "El usuario no existe");
            return "redirect:/usuarios";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") Usuario form, BindingResult result, Model model,
            Principal principal, RedirectAttributes ra) {
        boolean esNuevo = form.getId() == null;
        Usuario usuario = esNuevo ? new Usuario() : usuarioRepository.findById(form.getId()).orElse(null);
        if (usuario == null) {
            ra.addFlashAttribute("error", "El usuario no existe");
            return "redirect:/usuarios";
        }

        String correo = form.getCorreo() == null ? null : form.getCorreo().toLowerCase();
        if (correo != null && (esNuevo ? usuarioRepository.existsByCorreo(correo)
                : usuarioRepository.existsByCorreoAndIdNot(correo, form.getId()))) {
            result.rejectValue("correo", "duplicado", "Ese correo ya está registrado");
        }

        String clave = form.getPassword();
        if (esNuevo && clave == null) {
            result.rejectValue("password", "requerido", "La contraseña es obligatoria");
        } else if (clave != null && clave.length() < CLAVE_MINIMA) {
            result.rejectValue("password", "corta", "Mínimo " + CLAVE_MINIMA + " caracteres");
        }

        Propietario propietario = null;
        if (form.getRol() == Rol.PROPIETARIO) {
            Long propietarioId = form.getPropietario() == null ? null : form.getPropietario().getId();
            propietario = propietarioId == null ? null : propietarioRepository.findById(propietarioId).orElse(null);
            if (propietario == null) {
                result.rejectValue("propietario", "requerido", "Selecciona el propietario de esta cuenta");
            }
        }

        boolean esSuCuenta = !esNuevo && usuario.getCorreo().equals(principal.getName());
        if (esSuCuenta && (!form.isActivo() || form.getRol() != Rol.ADMIN)) {
            result.reject("propia", "No puedes desactivar tu cuenta ni quitarte el rol de administrador");
        }

        if (result.hasErrors()) {
            form.setPassword(null);
            cargarOpciones(model);
            return "usuarios/formulario";
        }

        usuario.setNombre(form.getNombre());
        usuario.setCorreo(correo);
        usuario.setRol(form.getRol());
        usuario.setActivo(form.isActivo());
        usuario.setPropietario(propietario);
        if (clave != null) {
            usuario.setPassword(passwordEncoder.encode(clave));
        }
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("ok", esNuevo ? "Usuario creado" : "Usuario actualizado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            ra.addFlashAttribute("error", "El usuario no existe");
        } else if (usuario.getCorreo().equals(principal.getName())) {
            ra.addFlashAttribute("error", "No puedes desactivar tu propia cuenta");
        } else {
            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
            ra.addFlashAttribute("ok", usuario.isActivo() ? "Usuario activado" : "Usuario desactivado");
        }
        return "redirect:/usuarios";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("roles", Rol.values());
        model.addAttribute("propietarios", propietarioRepository.findAllByOrderByNombreAsc());
    }
}
