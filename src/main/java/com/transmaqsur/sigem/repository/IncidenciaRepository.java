package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Incidencia;
import com.transmaqsur.sigem.model.enums.EstadoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findAllByOrderByFechaHoraDesc();

    List<Incidencia> findByUnidadIdOrderByFechaHoraDesc(Long unidadId);

    List<Incidencia> findByAsignacionIdOrderByFechaHoraDesc(Long asignacionId);

    long countByEstadoIn(Collection<EstadoIncidencia> estados);
}
