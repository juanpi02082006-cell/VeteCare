package com.Vetecare.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Producto del inventario. El stock solo cambia con movimientos de inventario,
 * para que cada unidad tenga trazabilidad.
 */
@Entity
@Table(name = "productos")
public class Producto {

    /** Un producto está "próximo a vencer" si vence dentro de estos días (spec.md, duda 5). */
    public static final int DIAS_AVISO_VENCIMIENTO = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "Selecciona la categoría")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaProducto categoria;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0", message = "No puede ser negativo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "Indica el stock")
    @PositiveOrZero(message = "No puede ser negativo")
    @Max(value = 100000, message = "Máximo 100000")
    @Column(nullable = false)
    private Integer stock = 0;

    @NotNull(message = "Indica el stock mínimo")
    @PositiveOrZero(message = "No puede ser negativo")
    @Max(value = 100000, message = "Máximo 100000")
    @Column(nullable = false)
    private Integer stockMinimo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimiento;

    /** RN y HU-08: crítico cuando el stock es menor o igual que el stock mínimo. */
    public boolean estaCritico() {
        return stock != null && stockMinimo != null && stock <= stockMinimo;
    }

    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean estaPorVencer() {
        return fechaVencimiento != null && !estaVencido()
                && !fechaVencimiento.isAfter(LocalDate.now().plusDays(DIAS_AVISO_VENCIMIENTO));
    }

    /** Días que faltan para el vencimiento (negativo si ya venció). */
    public Long getDiasParaVencer() {
        return fechaVencimiento == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaProducto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProducto categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
