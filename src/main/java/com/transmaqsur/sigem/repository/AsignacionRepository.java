package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    /** Asignaciones activas de la unidad que se cruzan con el rango [inicio, fin). */
    @Query("select a from Asignacion a where a.unidad.id = :unidadId and a.estado in :estados "
            + "and a.fechaInicio < :fin and a.fechaFin > :inicio and a.id <> :excluirId")
    List<Asignacion> cruceUnidad(Long unidadId, LocalDateTime inicio, LocalDateTime fin,
                                 Collection<EstadoAsignacion> estados, Long excluirId);

    /** Asignaciones activas del empleado que se cruzan con el rango [inicio, fin). */
    @Query("select a from Asignacion a where a.empleado.id = :empleadoId and a.estado in :estados "
            + "and a.fechaInicio < :fin and a.fechaFin > :inicio and a.id <> :excluirId")
    List<Asignacion> cruceEmpleado(Long empleadoId, LocalDateTime inicio, LocalDateTime fin,
                                   Collection<EstadoAsignacion> estados, Long excluirId);

    @Query("select a from Asignacion a where a.estado in :estados and a.fechaInicio < :fin and a.fechaFin > :inicio")
    List<Asignacion> activasEnRango(LocalDateTime inicio, LocalDateTime fin, Collection<EstadoAsignacion> estados);

    List<Asignacion> findAllByOrderByFechaInicioDesc();

    List<Asignacion> findByEstadoInOrderByFechaInicioAsc(Collection<EstadoAsignacion> estados);

    List<Asignacion> findByUnidadIdOrderByFechaInicioDesc(Long unidadId);

    List<Asignacion> findByEmpleadoIdOrderByFechaInicioDesc(Long empleadoId);

    List<Asignacion> findByEmpleadoIdAndEstadoInOrderByFechaInicioAsc(Long empleadoId, Collection<EstadoAsignacion> estados);

    long countByUnidadIdAndEstadoIn(Long unidadId, Collection<EstadoAsignacion> estados);
}
