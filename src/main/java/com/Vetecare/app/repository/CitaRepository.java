package com.Vetecare.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Vetecare.app.model.Cita;
import com.Vetecare.app.model.EstadoCita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * ¿El veterinario tiene otra cita en un estado dado que empiece entre desde y hasta (sin incluir los extremos)?
     * Dos citas de 30 minutos se cruzan si sus inicios están a menos de 30 minutos.
     */
    @Query("""
            select case when count(c) > 0 then true else false end from Cita c
            where c.veterinario.id = :veterinarioId
              and c.estado = :estado
              and c.fechaHora > :desde and c.fechaHora < :hasta
              and c.id <> :excluirId
            """)
    boolean existeCita(@Param("veterinarioId") Long veterinarioId, @Param("estado") EstadoCita estado,
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
            @Param("excluirId") Long excluirId);

    /** RN-02: solo las citas programadas ocupan el horario; las canceladas lo liberan. */
    default boolean veterinarioOcupado(Long veterinarioId, LocalDateTime inicio, Long citaExcluidaId) {
        return existeCita(veterinarioId, EstadoCita.PROGRAMADA,
                inicio.minusMinutes(Cita.DURACION_MINUTOS), inicio.plusMinutes(Cita.DURACION_MINUTOS),
                citaExcluidaId == null ? -1L : citaExcluidaId);
    }

    /**
     * Citas de un rango de fechas. Veterinario, estado y texto son filtros opcionales (null = todos);
     * el texto busca en el nombre de la mascota o del propietario.
     */
    @Query("""
            select c from Cita c
              join fetch c.mascota m join fetch m.propietario p
              join fetch c.veterinario v
            where c.fechaHora >= :desde and c.fechaHora < :hasta
              and (:veterinarioId is null or v.id = :veterinarioId)
              and (:estado is null or c.estado = :estado)
              and (:texto is null
                   or lower(m.nombre) like lower(concat('%', :texto, '%'))
                   or lower(p.nombre) like lower(concat('%', :texto, '%')))
            order by c.fechaHora
            """)
    List<Cita> buscar(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
            @Param("veterinarioId") Long veterinarioId, @Param("estado") EstadoCita estado,
            @Param("texto") String texto);

    long countByFechaHoraBetweenAndEstado(LocalDateTime desde, LocalDateTime hasta, EstadoCita estado);

    long countByVeterinarioIdAndFechaHoraBetweenAndEstado(Long veterinarioId, LocalDateTime desde,
            LocalDateTime hasta, EstadoCita estado);
}
