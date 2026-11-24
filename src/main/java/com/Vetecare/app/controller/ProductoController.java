package com.Vetecare.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

import com.Vetecare.app.model.CategoriaProducto;
import com.Vetecare.app.model.MovimientoInventario;
import com.Vetecare.app.model.Producto;
import com.Vetecare.app.model.TipoMovimiento;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.DetalleConsultaRepository;
import com.Vetecare.app.repository.MovimientoInventarioRepository;
import com.Vetecare.app.repository.ProductoRepository;
import com.Vetecare.app.repository.UsuarioRepository;

import jakarta.validation.Valid;

/** HU-07 (catálogo de productos) y HU-08 (alertas de stock y vencimiento). */
@Controller
@RequestMapping("/inventario")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final DetalleConsultaRepository detalleRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductoController(ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository,
            DetalleConsultaRepository detalleRepository, UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.detalleRepository = detalleRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/productos")
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("productos", q == null ? productoRepository.findAllByOrderByNombreAsc()
                : productoRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc(q));
        model.addAttribute("q", q);
        return "inventario/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", CategoriaProducto.values());
        return "inventario/producto-form";
    }

    @GetMapping("/productos/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productoRepository.findById(id).map(producto -> {
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", CategoriaProducto.values());
            return "inventario/producto-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "El producto no existe");
            return "redirect:/inventario/productos";
        });
    }

    /**
     * Al crear, el stock inicial queda como un movimiento de entrada. Al editar, el stock
     * no se toca: solo cambia con entradas y salidas.
     */
    @PostMapping("/productos/guardar")
    @Transactional
    public String guardar(@Valid @ModelAttribute("producto") Producto form, BindingResult result, Model model,
            Principal principal, RedirectAttributes ra) {
        boolean esNuevo = form.getId() == null;
        Usuario usuario = principal == null ? null : usuarioRepository.findByCorreo(principal.getName()).orElse(null);
        if (esNuevo && usuario == null) {
            result.reject("usuario", "No se encontró tu cuenta de usuario");
        }
        if (result.hasErrors()) {
            model.addAttribute("categorias", CategoriaProducto.values());
            return "inventario/producto-form";
        }

        Producto producto;
        if (esNuevo) {
            producto = form;
            productoRepository.save(producto);
            if (producto.getStock() > 0) {
                MovimientoInventario entrada = new MovimientoInventario();
                entrada.setProducto(producto);
                entrada.setUsuario(usuario);
                entrada.setTipo(TipoMovimiento.ENTRADA);
                entrada.setCantidad(producto.getStock());
                entrada.setMotivo("Stock inicial");
                entrada.setFechaMovimiento(LocalDateTime.now());
                movimientoRepository.save(entrada);
            }
        } else {
            producto = productoRepository.findById(form.getId()).orElse(null);
            if (producto == null) {
                ra.addFlashAttribute("error", "El producto no existe");
                return "redirect:/inventario/productos";
            }
            producto.setNombre(form.getNombre());
            producto.setCategoria(form.getCategoria());
            producto.setPrecio(form.getPrecio());
            producto.setStockMinimo(form.getStockMinimo());
            producto.setFechaVencimiento(form.getFechaVencimiento());
            productoRepository.save(producto);
        }
        ra.addFlashAttribute("ok", esNuevo ? "Producto registrado" : "Producto actualizado");
        return "redirect:/inventario/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        if (movimientoRepository.existsByProductoId(id) || detalleRepository.existsByProductoId(id)) {
            ra.addFlashAttribute("error", "No se puede eliminar: tiene movimientos registrados");
        } else if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            ra.addFlashAttribute("ok", "Producto eliminado");
        }
        return "redirect:/inventario/productos";
    }

    /** HU-08: stock crítico, vencidos y próximos a vencer. */
    @GetMapping("/alertas")
    public String alertas(Model model) {
        List<Producto> conVencimiento = productoRepository
                .conVencimientoHasta(LocalDate.now().plusDays(Producto.DIAS_AVISO_VENCIMIENTO));
        model.addAttribute("criticos", productoRepository.criticos());
        model.addAttribute("vencidos", conVencimiento.stream().filter(Producto::estaVencido).toList());
        model.addAttribute("porVencer", conVencimiento.stream().filter(Producto::estaPorVencer).toList());
        model.addAttribute("diasAviso", Producto.DIAS_AVISO_VENCIMIENTO);
        return "inventario/alertas";
    }
}
