package com.Vetecare.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Vetecare.app.model.DetalleConsulta;

public interface DetalleConsultaRepository extends JpaRepository<DetalleConsulta, Long> {

    @EntityGraph(attributePaths = "producto")
    List<DetalleConsulta> findByConsultaIdOrderByIdAsc(Long consultaId);

    boolean existsByProductoId(Long productoId);
}
