package com.transur.sigem.mantenimiento.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    List<Mantenimiento> findAllByOrderByIdDesc();

    List<Mantenimiento> findByMaquinariaIdOrderByIdDesc(Long maquinariaId);

    List<Mantenimiento> findByEstadoIn(Collection<String> estados);

    /** Órdenes abiertas de la unidad programadas dentro del rango (RF009). */
    List<Mantenimiento> findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(Long maquinariaId, Collection<String> estados,
                                                                              LocalDate desde, LocalDate hasta);

    boolean existsByMaquinariaIdAndEstado(Long maquinariaId, String estado);
}