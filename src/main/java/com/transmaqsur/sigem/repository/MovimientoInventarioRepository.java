package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("select m from MovimientoInventario m where (:almacenId is null or m.almacen.id = :almacenId) and (:repuestoId is null or m.repuesto.id = :repuestoId) order by m.fecha desc, m.id desc")
    Page<MovimientoInventario> buscar(Long almacenId, Long repuestoId, Pageable pageable);
}
