package com.Vetecare.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.Vetecare.app.model.MovimientoInventario;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = { "producto", "usuario" })
    List<MovimientoInventario> findTop100ByOrderByFechaMovimientoDescIdDesc();

    @EntityGraph(attributePaths = { "producto", "usuario" })
    List<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDescIdDesc(Long productoId);

    boolean existsByProductoId(Long productoId);
}
