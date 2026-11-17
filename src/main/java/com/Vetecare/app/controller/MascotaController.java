package com.Vetecare.app.controller;

import java.util.List;

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

import com.Vetecare.app.model.Especie;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.Propietario;
import com.Vetecare.app.model.Sexo;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.PropietarioRepository;

import jakarta.validation.Valid;

/** HU-03: la recepcionista registra mascotas asociadas a un propietario existente. */
@Controller
@RequestMapping("/mascotas")
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;

    public MascotaController(MascotaRepository mascotaRepository, PropietarioRepository propietarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long propietarioId, Model model) {
        List<Mascota> mascotas;
        if (propietarioId != null) {
            mascotas = mascotaRepository.findByPropietarioIdOrderByNombreAsc(propietarioId);
            model.addAttribute("propietarioFiltro", propietarioRepository.findById(propietarioId).orElse(null));
        } else if (q != null) {
            mascotas = mascotaRepository.buscar(q);
        } else {
            mascotas = mascotaRepository.findAllByOrderByNombreAsc();
        }
        model.addAttribute("mascotas", mascotas);
        model.addAttribute("q", q);
        return "mascotas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long propietarioId, Model model) {
        Mascota mascota = new Mascota();
        if (propietarioId != null) {
            propietarioRepository.findById(propietarioId).ifPresent(mascota::setPropietario);
        }
        model.addAttribute("mascota", mascota);
        cargarOpciones(model);
        return "mascotas/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return mascotaRepository.findById(id).map(mascota -> {
            model.addAttribute("mascota", mascota);
            cargarOpciones(model);
            return "mascotas/formulario";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "La mascota no existe");
            return "redirect:/mascotas";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("mascota") Mascota mascota, BindingResult result, Model model,
            RedirectAttributes ra) {
        // El formulario solo envía el id del propietario: se busca el registro real en la base de datos.
        Long propietarioId = mascota.getPropietario() == null ? null : mascota.getPropietario().getId();
        Propietario propietario = propietarioId == null ? null
                : propietarioRepository.findById(propietarioId).orElse(null);
        if (propietario == null) {
            result.rejectValue("propietario", "requerido", "Selecciona un propietario existente");
        } else {
            mascota.setPropietario(propietario);
        }

        if (result.hasErrors()) {
            cargarOpciones(model);
            return "mascotas/formulario";
        }

        boolean esNuevo = mascota.getId() == null;
        mascotaRepository.save(mascota);
        ra.addFlashAttribute("ok", esNuevo ? "Mascota registrada" : "Mascota actualizada");
        return "redirect:/mascotas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        if (mascotaRepository.existsById(id)) {
            mascotaRepository.deleteById(id);
            ra.addFlashAttribute("ok", "Mascota eliminada");
        } else {
            ra.addFlashAttribute("error", "La mascota no existe");
        }
        return "redirect:/mascotas";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("propietarios", propietarioRepository.findAllByOrderByNombreAsc());
        model.addAttribute("especies", Especie.values());
        model.addAttribute("sexos", Sexo.values());
    }
}
