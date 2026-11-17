package com.Vetecare.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Vetecare.app.model.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    @EntityGraph(attributePaths = "propietario")
    List<Mascota> findAllByOrderByNombreAsc();

    @EntityGraph(attributePaths = "propietario")
    List<Mascota> findByPropietarioIdOrderByNombreAsc(Long propietarioId);

    /** Busca por nombre de la mascota o nombre del propietario. */
    @Query("""
            select m from Mascota m join fetch m.propietario p
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
               or lower(p.nombre) like lower(concat('%', :texto, '%'))
            order by m.nombre
            """)
    List<Mascota> buscar(@Param("texto") String texto);

    boolean existsByPropietarioId(Long propietarioId);

    /** Filas [propietarioId, cantidad de mascotas]. */
    @Query("select m.propietario.id, count(m) from Mascota m group by m.propietario.id")
    List<Object[]> contarPorPropietario();
}
