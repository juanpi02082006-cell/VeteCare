package com.Vetecare.app.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cita de una mascota con un veterinario. Dura {@link #DURACION_MINUTOS} minutos. */
@Entity
@Table(name = "citas")
public class Cita {

    /** Duración usada para detectar cruces de horario (spec.md, duda 4). */
    public static final int DURACION_MINUTOS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Indica la fecha y la hora")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 200, message = "Máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    /** Quién registró la cita. */
    @ManyToOne
    @JoinColumn(name = "recepcionista_id")
    private Usuario recepcionista;

    public LocalDateTime getFechaFin() {
        return fechaHora == null ? null : fechaHora.plusMinutes(DURACION_MINUTOS);
    }

    public boolean isProgramada() {
        return estado == EstadoCita.PROGRAMADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Usuario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Usuario veterinario) {
        this.veterinario = veterinario;
    }

    public Usuario getRecepcionista() {
        return recepcionista;
    }

    public void setRecepcionista(Usuario recepcionista) {
        this.recepcionista = recepcionista;
    }
}
