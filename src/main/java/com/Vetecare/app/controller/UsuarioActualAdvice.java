package com.Vetecare.app.controller;

import java.security.Principal;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.UsuarioRepository;

/** Comportamiento común a todos los controladores. */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Recorta espacios y convierte los textos vacíos en null antes de validar. */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** Usuario con sesión iniciada, para mostrar su nombre y rol en el menú. */
    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(principal.getName()).orElse(null);
    }
}
