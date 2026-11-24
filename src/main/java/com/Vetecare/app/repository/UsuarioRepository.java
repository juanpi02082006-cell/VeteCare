package com.Vetecare.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Vetecare.app.model.Rol;
import com.Vetecare.app.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByPropietarioId(Long propietarioId);

    @EntityGraph(attributePaths = "propietario")
    List<Usuario> findAllByOrderByNombreAsc();

    /** Por ejemplo, los veterinarios activos para agendar citas. */
    List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(Rol rol);
}
