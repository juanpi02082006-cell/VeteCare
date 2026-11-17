package com.Vetecare.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Vetecare.app.model.Propietario;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;

import jakarta.validation.Valid;

/** HU-02: la recepcionista registra y actualiza los datos de los propietarios. */
@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public PropietarioController(PropietarioRepository propietarioRepository, MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository) {
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        List<Propietario> propietarios = q == null
                ? propietarioRepository.findAllByOrderByNombreAsc()
                : propietarioRepository.findByNombreContainingIgnoreCaseOrDocumentoContainingOrderByNombreAsc(q, q);

        Map<Long, Long> mascotasPorPropietario = new HashMap<>();
        for (Object[] fila : mascotaRepository.contarPorPropietario()) {
            mascotasPorPropietario.put((Long) fila[0], (Long) fila[1]);
        }

        model.addAttribute("propietarios", propietarios);
        model.addAttribute("mascotasPorPropietario", mascotasPorPropietario);
        model.addAttribute("q", q);
        return "propietarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("propietario", new Propietario());
        return "propietarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return propietarioRepository.findById(id).map(propietario -> {
            model.addAttribute("propietario", propietario);
            return "propietarios/formulario";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "El propietario no existe");
            return "redirect:/propietarios";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("propietario") Propietario propietario, BindingResult result,
            RedirectAttributes ra) {
        String documento = propietario.getDocumento();
        if (documento != null && (propietario.getId() == null
                ? propietarioRepository.existsByDocumento(documento)
                : propietarioRepository.existsByDocumentoAndIdNot(documento, propietario.getId()))) {
            result.rejectValue("documento", "duplicado", "Ya existe un propietario con ese documento");
        }
        if (result.hasErrors()) {
            return "propietarios/formulario";
        }

        if (propietario.getCorreo() != null) {
            propietario.setCorreo(propietario.getCorreo().toLowerCase());
        }
        boolean esNuevo = propietario.getId() == null;
        propietarioRepository.save(propietario);
        ra.addFlashAttribute("ok", esNuevo ? "Propietario registrado" : "Propietario actualizado");
        return "redirect:/propietarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        if (mascotaRepository.existsByPropietarioId(id)) {
            ra.addFlashAttribute("error", "No se puede eliminar: tiene mascotas registradas");
        } else if (usuarioRepository.existsByPropietarioId(id)) {
            ra.addFlashAttribute("error", "No se puede eliminar: tiene una cuenta de usuario");
        } else {
            propietarioRepository.deleteById(id);
            ra.addFlashAttribute("ok", "Propietario eliminado");
        }
        return "redirect:/propietarios";
    }
}
