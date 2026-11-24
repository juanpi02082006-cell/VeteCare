package com.Vetecare.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Vetecare.app.model.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByCitaId(Long citaId);

    Optional<Consulta> findByCitaId(Long citaId);

    long countByCitaVeterinarioId(Long veterinarioId);

    @Query("""
            select co from Consulta co
              join fetch co.cita c join fetch c.mascota m join fetch m.propietario join fetch c.veterinario
            where c.veterinario.id = :veterinarioId
            order by co.fechaRegistro desc
            """)
    List<Consulta> deVeterinario(@Param("veterinarioId") Long veterinarioId);

    /** Historial clínico de una mascota, de la más reciente a la más antigua. */
    @Query("""
            select co from Consulta co
              join fetch co.cita c join fetch c.mascota m join fetch c.veterinario
            where m.id = :mascotaId
            order by c.fechaHora desc
            """)
    List<Consulta> deMascota(@Param("mascotaId") Long mascotaId);
}
