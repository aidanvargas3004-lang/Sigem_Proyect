package com.transur.sigem.mantenimiento.repository;

import com.transur.sigem.mantenimiento.entity.PlanMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanMantenimientoRepository extends JpaRepository<PlanMantenimiento, Long> {

    List<PlanMantenimiento> findByActivoTrue();

    List<PlanMantenimiento> findByMaquinariaId(Long maquinariaId);
}
