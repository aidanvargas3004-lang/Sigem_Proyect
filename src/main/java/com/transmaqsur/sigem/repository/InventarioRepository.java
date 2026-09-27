package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByAlmacenIdAndRepuestoId(Long almacenId, Long repuestoId);

    List<Inventario> findByAlmacenIdOrderByRepuestoNombreAsc(Long almacenId);

    List<Inventario> findByRepuestoIdOrderByAlmacenNombreAsc(Long repuestoId);

    List<Inventario> findAllByOrderByAlmacenNombreAscRepuestoNombreAsc();

    @Query("select coalesce(sum(i.cantidad), 0) from Inventario i where i.repuesto.id = :repuestoId")
    BigDecimal stockTotal(Long repuestoId);

    @Query("select i from Inventario i where i.repuesto.activo = true and i.cantidad <= i.repuesto.stockMinimo order by i.repuesto.nombre")
    List<Inventario> findBajoMinimo();

    @Query("select i from Inventario i where i.cantidad > 0 order by i.repuesto.nombre, i.almacen.nombre")
    List<Inventario> findConStock();
}
