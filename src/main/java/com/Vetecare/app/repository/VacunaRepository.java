package com.Vetecare.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Vetecare.app.model.Vacuna;

public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    @EntityGraph(attributePaths = { "mascota", "mascota.propietario", "veterinario" })
    List<Vacuna> findAllByOrderByFechaRefuerzoAsc();

    @EntityGraph(attributePaths = { "mascota", "veterinario" })
    List<Vacuna> findByMascotaIdOrderByFechaAplicacionDesc(Long mascotaId);
}
