package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoContrato;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findAllByOrderByIdDesc();

    List<Contrato> findByEstadoOrderByFechaFinAsc(EstadoContrato estado);

    List<Contrato> findByClienteIdOrderByIdDesc(Long clienteId);

    List<Contrato> findByEstadoAndFechaFinBefore(EstadoContrato estado, LocalDate fecha);

    Optional<Contrato> findFirstByCotizacionId(Long cotizacionId);

    long countByEstado(EstadoContrato estado);
}
