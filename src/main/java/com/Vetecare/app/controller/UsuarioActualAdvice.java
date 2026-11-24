package com.Vetecare.app.controller;

import java.security.Principal;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.UsuarioRepository;

/** Comportamiento común a todos los controladores. */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public UsuarioActualAdvice(UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
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

    /** Número de alertas de inventario para el contador del menú (solo auxiliar). */
    @ModelAttribute("alertasInventario")
    public Long alertasInventario(Principal principal) {
        if (principal instanceof Authentication autenticacion && autenticacion.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUXILIAR"))) {
            return productoRepository.contarAlertas();
        }
        return null;
    }
}
