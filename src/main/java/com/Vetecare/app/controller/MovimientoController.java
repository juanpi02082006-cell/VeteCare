package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Vetecare.app.model.MovimientoInventario;
import com.Vetecare.app.model.Producto;
import com.Vetecare.app.model.TipoMovimiento;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.MovimientoInventarioRepository;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.UsuarioRepository;

import jakarta.validation.Valid;

/** HU-07: entradas y salidas de inventario sin dejar el stock en negativo. */
@Controller
@RequestMapping("/inventario/movimientos")
public class MovimientoController {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoController(MovimientoInventarioRepository movimientoRepository,
            ProductoRepository productoRepository, UsuarioRepository usuarioRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long productoId, Model model) {
        if (productoId != null) {
            model.addAttribute("movimientos", movimientoRepository.findByProductoIdOrderByFechaMovimientoDescIdDesc(productoId));
            model.addAttribute("productoFiltro", productoRepository.findById(productoId).orElse(null));
        } else {
            model.addAttribute("movimientos", movimientoRepository.findTop100ByOrderByFechaMovimientoDescIdDesc());
        }
        return "inventario/movimientos";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long productoId,
            @RequestParam(required = false) TipoMovimiento tipo, Model model) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(tipo == null ? TipoMovimiento.ENTRADA : tipo);
        if (productoId != null) {
            productoRepository.findById(productoId).ifPresent(movimiento::setProducto);
        }
        model.addAttribute("movimiento", movimiento);
        cargarOpciones(model);
        return "inventario/movimiento-form";
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardar(@Valid @ModelAttribute("movimiento") MovimientoInventario form, BindingResult result,
            Model model, Principal principal, RedirectAttributes ra) {
        Long productoId = form.getProducto() == null ? null : form.getProducto().getId();
        Producto producto = productoId == null ? null : productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            result.rejectValue("producto", "requerido", "Selecciona un producto");
        }
        Usuario usuario = principal == null ? null : usuarioRepository.findByCorreo(principal.getName()).orElse(null);
        if (usuario == null) {
            result.reject("usuario", "No se encontró tu cuenta de usuario");
        }
        // RN-07 / AC-020: una salida no puede superar el stock disponible
        if (producto != null && form.getTipo() == TipoMovimiento.SALIDA && form.getCantidad() != null
                && form.getCantidad() > producto.getStock()) {
            result.rejectValue("cantidad", "stock", "Stock insuficiente: hay " + producto.getStock() + " unidades");
        }
        if (result.hasErrors()) {
            cargarOpciones(model);
            return "inventario/movimiento-form";
        }

        int cantidad = form.getCantidad();
        producto.setStock(form.getTipo() == TipoMovimiento.ENTRADA
                ? producto.getStock() + cantidad
                : producto.getStock() - cantidad);
        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(form.getTipo());
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(form.getMotivo());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        ra.addFlashAttribute("ok", form.getTipo().getEtiqueta() + " registrada · " + producto.getNombre()
                + " queda con " + producto.getStock());
        return "redirect:/inventario/movimientos";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("productos", productoRepository.findAllByOrderByNombreAsc());
        model.addAttribute("tipos", TipoMovimiento.values());
    }
}
