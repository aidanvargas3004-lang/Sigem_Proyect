package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.OrdenMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoOrden;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface OrdenMantenimientoRepository extends JpaRepository<OrdenMantenimiento, Long> {

    List<OrdenMantenimiento> findAllByOrderByIdDesc();

    List<OrdenMantenimiento> findByUnidadIdOrderByIdDesc(Long unidadId);

    List<OrdenMantenimiento> findByTecnicoIdAndEstadoInOrderByFechaProgramadaAsc(Long tecnicoId, Collection<EstadoOrden> estados);

    List<OrdenMantenimiento> findByEstadoInOrderByFechaProgramadaAsc(Collection<EstadoOrden> estados);

    boolean existsByUnidadIdAndEstadoIn(Long unidadId, Collection<EstadoOrden> estados);

    long countByEstadoIn(Collection<EstadoOrden> estados);

    /** Órdenes abiertas de la unidad programadas dentro del rango de fechas. */
    @Query("select o from OrdenMantenimiento o where o.unidad.id = :unidadId and o.estado in :estados "
            + "and o.fechaProgramada between :desde and :hasta")
    List<OrdenMantenimiento> programadasEnRango(Long unidadId, LocalDate desde, LocalDate hasta, Collection<EstadoOrden> estados);
}
