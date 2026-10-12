package com.transur.sigem.mantenimiento.repository;

import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    // Puedes agregar búsquedas por maquinaria o estado si lo necesitas
}