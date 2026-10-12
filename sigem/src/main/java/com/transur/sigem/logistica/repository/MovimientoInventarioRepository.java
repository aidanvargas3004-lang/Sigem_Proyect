package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByRepuestoIdOrderByFechaDesc(Long repuestoId);

    List<MovimientoInventario> findAllByOrderByFechaDesc();
}