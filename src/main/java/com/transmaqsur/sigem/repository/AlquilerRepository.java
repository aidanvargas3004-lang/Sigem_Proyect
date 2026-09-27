package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;

import java.util.Optional;
import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    List<Alquiler> findAllByOrderByIdDesc();

    List<Alquiler> findByContratoIdOrderByIdDesc(Long contratoId);

    List<Alquiler> findByClienteIdAndEstadoAndFacturadoFalseOrderByIdAsc(Long clienteId, EstadoOperacion estado);

    List<Alquiler> findByEstadoAndFacturadoFalseOrderByIdAsc(EstadoOperacion estado);

    Optional<Alquiler> findByAsignacionId(Long asignacionId);

    long countByEstado(EstadoOperacion estado);
}
