package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.ServicioTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;

import java.util.List;
import java.util.Optional;

public interface ServicioTransporteRepository extends JpaRepository<ServicioTransporte, Long> {

    List<ServicioTransporte> findAllByOrderByIdDesc();

    List<ServicioTransporte> findByContratoIdOrderByIdDesc(Long contratoId);

    List<ServicioTransporte> findByClienteIdAndEstadoAndFacturadoFalseOrderByIdAsc(Long clienteId, EstadoOperacion estado);

    List<ServicioTransporte> findByEstadoAndFacturadoFalseOrderByIdAsc(EstadoOperacion estado);

    Optional<ServicioTransporte> findByAsignacionId(Long asignacionId);

    long countByEstado(EstadoOperacion estado);
}
