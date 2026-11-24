package com.Vetecare.app.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/** Vacuna aplicada a una mascota. Una mascota puede tener muchas (RN-05). */
@Entity
@Table(name = "vacunas")
public class Vacuna {

    /** Un refuerzo es "próximo" si vence dentro de estos días (spec.md, duda 6). */
    public static final int DIAS_AVISO_REFUERZO = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @NotBlank(message = "El nombre de la vacuna es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotNull(message = "Indica la fecha de aplicación")
    @PastOrPresent(message = "No puede ser una fecha futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fechaAplicacion;

    @NotNull(message = "Indica el intervalo")
    @Min(value = 1, message = "Mínimo 1 día")
    @Max(value = 1095, message = "Máximo 1095 días (3 años)")
    @Column(nullable = false)
    private Integer intervaloDias;

    @Column(nullable = false)
    private LocalDate fechaRefuerzo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    /** RN-06: fecha de refuerzo = fecha de aplicación + intervalo en días. */
    public LocalDate calcularFechaRefuerzo() {
        return fechaAplicacion.plusDays(intervaloDias);
    }

    public boolean isRefuerzoVencido() {
        return fechaRefuerzo != null && fechaRefuerzo.isBefore(LocalDate.now());
    }

    public boolean isRefuerzoProximo() {
        return fechaRefuerzo != null && !isRefuerzoVencido()
                && !fechaRefuerzo.isAfter(LocalDate.now().plusDays(DIAS_AVISO_REFUERZO));
    }

    /**
     * Ids de las vacunas que ya fueron reforzadas: existe una aplicación más reciente
     * de la misma vacuna para la misma mascota, así que su refuerzo ya no está pendiente.
     */
    public static Set<Long> idsReforzadas(Collection<Vacuna> vacunas) {
        Map<String, Vacuna> masReciente = new HashMap<>();
        for (Vacuna v : vacunas) {
            String clave = v.getMascota().getId() + "|" + v.getNombre().trim().toLowerCase();
            Vacuna actual = masReciente.get(clave);
            if (actual == null || v.getFechaAplicacion().isAfter(actual.getFechaAplicacion())) {
                masReciente.put(clave, v);
            }
        }
        Set<Long> reforzadas = new HashSet<>();
        for (Vacuna v : vacunas) {
            String clave = v.getMascota().getId() + "|" + v.getNombre().trim().toLowerCase();
            if (masReciente.get(clave) != v) {
                reforzadas.add(v.getId());
            }
        }
        return reforzadas;
    }

    /** Vacunas con refuerzo vencido o próximo que todavía no se han vuelto a aplicar. */
    public static List<Vacuna> pendientes(List<Vacuna> vacunas) {
        Set<Long> reforzadas = idsReforzadas(vacunas);
        return vacunas.stream()
                .filter(v -> !reforzadas.contains(v.getId()))
                .filter(v -> v.isRefuerzoVencido() || v.isRefuerzoProximo())
                .toList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public Integer getIntervaloDias() {
        return intervaloDias;
    }

    public void setIntervaloDias(Integer intervaloDias) {
        this.intervaloDias = intervaloDias;
    }

    public LocalDate getFechaRefuerzo() {
        return fechaRefuerzo;
    }

    public void setFechaRefuerzo(LocalDate fechaRefuerzo) {
        this.fechaRefuerzo = fechaRefuerzo;
    }

    public Usuario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Usuario veterinario) {
        this.veterinario = veterinario;
    }
}
