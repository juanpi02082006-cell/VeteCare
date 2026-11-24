package com.Vetecare.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Vetecare.app.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByNombreAsc();

    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    /** HU-08: stock menor o igual que el stock mínimo. */
    @Query("select p from Producto p where p.stock <= p.stockMinimo order by p.stock, p.nombre")
    List<Producto> criticos();

    /** Productos que vencen hasta la fecha límite (incluye los ya vencidos). */
    @Query("""
            select p from Producto p
            where p.fechaVencimiento is not null and p.fechaVencimiento <= :limite
            order by p.fechaVencimiento
            """)
    List<Producto> conVencimientoHasta(@Param("limite") LocalDate limite);

    @Query("""
            select count(p) from Producto p
            where p.stock <= p.stockMinimo
               or (p.fechaVencimiento is not null and p.fechaVencimiento <= :limite)
            """)
    long contarConAlerta(@Param("limite") LocalDate limite);

    /** Número de productos con alguna alerta (crítico, vencido o próximo a vencer). */
    default long contarAlertas() {
        return contarConAlerta(LocalDate.now().plusDays(Producto.DIAS_AVISO_VENCIMIENTO));
    }

    /** RN-08: productos que se pueden usar en una consulta (con stock y sin vencer). */
    @Query("""
            select p from Producto p
            where p.stock > 0 and (p.fechaVencimiento is null or p.fechaVencimiento >= :hoy)
            order by p.nombre
            """)
    List<Producto> disponibles(@Param("hoy") LocalDate hoy);
}
