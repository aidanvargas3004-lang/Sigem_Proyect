package com.transur.sigem.mantenimiento.repository;

import com.transur.sigem.mantenimiento.entity.MantenimientoRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoRepuestoRepository extends JpaRepository<MantenimientoRepuesto, Long> {
}