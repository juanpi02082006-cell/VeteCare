package com.Vetecare.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Vetecare.app.model.Propietario;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    List<Propietario> findAllByOrderByNombreAsc();

    List<Propietario> findByNombreContainingIgnoreCaseOrDocumentoContainingOrderByNombreAsc(String nombre, String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
